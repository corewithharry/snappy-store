/** Notice of modification as required by the LGPL
 *  This file was modified by Gemstone Systems Inc. on
 *  $Date$
 **/
// $Id: UDP.java,v 1.109 2005/12/23 17:08:22 belaban Exp $

package com.gemstone.org.jgroups.protocols;



import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.gemstone.org.jgroups.Address;
import com.gemstone.org.jgroups.Event;
import com.gemstone.org.jgroups.JChannel;
import com.gemstone.org.jgroups.Message;
import com.gemstone.org.jgroups.SuspectMember;
import com.gemstone.org.jgroups.protocols.UNICAST.UnicastHeader;
import com.gemstone.org.jgroups.protocols.pbcast.GMS.GmsHeader;
import com.gemstone.org.jgroups.stack.IpAddress;
import com.gemstone.org.jgroups.util.BoundedList;
import com.gemstone.org.jgroups.util.ExternalStrings;
import com.gemstone.org.jgroups.util.GemFireTracer;
import com.gemstone.org.jgroups.util.StringId;
import com.gemstone.org.jgroups.util.Util;


/**
 * IP multicast transport based on UDP. Messages to the group (msg.dest == null) will
 * be multicast (to all group members), whereas point-to-point messages
 * (msg.dest != null) will be unicast to a single member. Uses a multicast and
 * a unicast socket.<p>
 * The following properties are read by the UDP protocol:
 * <ul>
 * <li> param mcast_addr - the multicast address to use; default is 228.8.8.8.
 * <li> param mcast_port - (int) the port that the multicast is sent on; default is 7600
 * <li> param ip_mcast - (boolean) flag whether to use IP multicast; default is true.
 * <li> param ip_ttl - the default time-to-live for multicast packets sent out on this
 * socket; default is 32.
 * <li> param use_packet_handler - boolean, defaults to false.  
 * If set, the mcast and ucast receiver threads just put
 * the datagram's payload (a byte buffer) into a queue, from where a separate thread
 * will dequeue and handle them (unmarshal and pass up). This frees the receiver
 * threads from having to do message unmarshalling; this time can now be spent
 * receiving packets. If you have lots of retransmissions because of network
 * input buffer overflow, consider setting this property to true.
 * </ul>
 * @author Bela Ban
 */
public class UDP extends TP implements Runnable {

    /** Socket used for
     * <ol>
     * <li>sending unicast packets and
     * <li>receiving unicast packets
     * </ol>
     * The address of this socket will be our local address (<tt>local_addr</tt>) */
    DatagramSocket  sock=null;

    /**
     * BoundedList<Integer> of the last 100 ports used. This is to avoid reusing a port for DatagramSocket
     */
    private static BoundedList last_ports_used=null;

    /** Maintain a list of local ports opened by DatagramSocket. If this is 0, this option is turned off.
     * If bind_port is > 0, then this option will be ignored */
    int             num_last_ports=100;

    /** IP multicast socket for <em>receiving</em> multicast packets */
    MulticastSocket mcast_recv_sock=null;

    /** IP multicast socket for <em>sending</em> multicast packets */
    MulticastSocket mcast_send_sock=null;

    /** If we have multiple mcast send sockets, e.g. send_interfaces or send_on_all_interfaces enabled */
    MulticastSocket[] mcast_send_sockets=null;
    
    int[] membership_port_range = new int[]{0,65535};

    /**
     * Traffic class for sending unicast and multicast datagrams.
     * Valid values are (check {@link DatagramSocket#setTrafficClass(int)}  for details):
     * <UL>
     * <LI><CODE>IPTOS_LOWCOST (0x02)</CODE>, <b>decimal 2</b></LI>
     * <LI><CODE>IPTOS_RELIABILITY (0x04)</CODE><, <b>decimal 4</b>/LI>
     * <LI><CODE>IPTOS_THROUGHPUT (0x08)</CODE>, <b>decimal 8</b></LI>
     * <LI><CODE>IPTOS_LOWDELAY (0x10)</CODE>, <b>decimal</b> 16</LI>
     * </UL>
     */
    int             tos=0; // valid values: 2, 4, 8, 16


    /** The multicast address (mcast address and port) this member uses */
    IpAddress       mcast_addr=null;

    /** The multicast address used for sending and receiving packets */
    String          mcast_addr_name="228.8.8.8";

    /** The multicast port used for sending and receiving packets */
    int             mcast_port=7600;

    /** The multicast receiver thread */
    Thread          mcast_receiver=null; // GemStoneAddition -- accesses synchronized on this

    /** The unicast receiver thread */
    UcastReceiver   ucast_receiver=null;

    /** Whether to enable IP multicasting. If false, multiple unicast datagram
     * packets are sent rather than one multicast packet */
    boolean         ip_mcast=true;

    /** The time-to-live (TTL) for multicast datagram packets */
    int             ip_ttl=64;

    /** Send buffer size of the multicast datagram socket */
    int             mcast_send_buf_size=32000;

    /** Receive buffer size of the multicast datagram socket */
    int             mcast_recv_buf_size=64000;

    /** Send buffer size of the unicast datagram socket */
    int             ucast_send_buf_size=32000;

    /** Receive buffer size of the unicast datagram socket */
    int             ucast_recv_buf_size=64000;


    /** Usually, src addresses are nulled, and the receiver simply sets them to the address of the sender. However,
     * for multiple addresses on a Windows loopback device, this doesn't work
     * (see http://jira.jboss.com/jira/browse/JGRP-79 and the JGroups wiki for details). This must be the same
     * value for all members of the same group. Default is true, for performance reasons */
    // private boolean null_src_addresses=true;

    // GemStoneAddition - control a closed UDP protocol
    boolean closed = false;

    // GemStoneAddition - see bug 40886
    /** 
      * On OSes that support it: bind to the mcast_addr to prevent traffic on 
      * different addresses, but on the same port.
      * "-Dgemfire.jg-can_bind_to_mcast_addr=true" can be used if an OS needs to
      * avoid cross address chatter.
      */
    private boolean can_bind_to_mcast_addr = 
                  Boolean.getBoolean("gemfire.jg-can_bind_to_mcast_addr") || 
                  Util.checkForLinux() || 
                  Util.checkForSolaris() ||
                  Util.checkForAIX();

    /**
     * Creates the UDP protocol, and initializes the
     * state variables, does however not start any sockets or threads.
     */
    public UDP() {
    }


    /**
     * Setup the Protocol instance acording to the configuration string.
     * The following properties are read by the UDP protocol:
     * <ul>
     * <li> param mcast_addr - the multicast address to use default is 228.8.8.8
     * <li> param mcast_port - (int) the port that the multicast is sent on default is 7600
     * <li> param ip_mcast - (boolean) flag whether to use IP multicast - default is true
     * <li> param ip_ttl - Set the default time-to-live for multicast packets sent out on this socket. default is 32
     * </ul>
     * @return true if no other properties are left.
     *         false if the properties still have data in them, ie ,
     *         properties are left over and not handled by the protocol stack
     */
    @Override // GemStoneAddition  
    public boolean setProperties(Properties props) {
        String str;

        if (!super.setProperties(props)) { // GemStoneAddition - don't continue if TP had trouble
          return false;
        }
        
        str=props.getProperty("membership_port_range_start");
        if (str != null) {
          membership_port_range[0] = Integer.parseInt(str);
          props.remove("membership_port_range_start");
        }

        str=props.getProperty("membership_port_range_end");
        if (str != null) {
          membership_port_range[1] = Integer.parseInt(str);
          props.remove("membership_port_range_end");
        }

        str=props.getProperty("num_last_ports");
        if(str != null) {
            num_last_ports=Integer.parseInt(str);
            props.remove("num_last_ports");
        }

        str=props.getProperty("mcast_addr");
        if(str != null) {
            mcast_addr_name=str;
            props.remove("mcast_addr");
        }

        str=System.getProperty("jboss.partition.udpGroup");
        if(str != null)
            mcast_addr_name=str;

        str=props.getProperty("mcast_port");
        if(str != null) {
            mcast_port=Integer.parseInt(str);
            props.remove("mcast_port");
        }
        str=System.getProperty("jboss.partition.udpPort");
        if(str != null)
            mcast_port=Integer.parseInt(str);

        str=props.getProperty("ip_mcast");
        if(str != null) {
            ip_mcast=Boolean.valueOf(str).booleanValue();
            props.remove("ip_mcast");
        }

        str=props.getProperty("ip_ttl");
        if(str != null) {
            ip_ttl=Integer.parseInt(str);
            props.remove("ip_ttl");
        }

        str=props.getProperty("tos");
        if(str != null) {
            tos=Integer.parseInt(str);
            props.remove("tos");
        }

        str=props.getProperty("mcast_send_buf_size");
        if(str != null) {
            mcast_send_buf_size=Integer.parseInt(str);
            props.remove("mcast_send_buf_size");
        }

        str=props.getProperty("mcast_recv_buf_size");
        if(str != null) {
            mcast_recv_buf_size=Integer.parseInt(str);
            props.remove("mcast_recv_buf_size");
        }

        str=props.getProperty("ucast_send_buf_size");
        if(str != null) {
            ucast_send_buf_size=Integer.parseInt(str);
            props.remove("ucast_send_buf_size");
        }

        str=props.getProperty("ucast_recv_buf_size");
        if(str != null) {
            ucast_recv_buf_size=Integer.parseInt(str);
            props.remove("ucast_recv_buf_size");
        }

        str=props.getProperty("null_src_addresses");
        if(str != null) {
            // null_src_addresses=Boolean.valueOf(str).booleanValue();
            props.remove("null_src_addresses");
            log.error(ExternalStrings.UDP_NULL_SRC_ADDRESSES_HAS_BEEN_DEPRECATED_PROPERTY_WILL_BE_IGNORED);
        }

        if(props.size() > 0) {
            log.error(ExternalStrings.UDP_THE_FOLLOWING_PROPERTIES_ARE_NOT_RECOGNIZED__0, props);
            return false;
        }
        return true;
    }

    /** for debugging unit tests we may need to see if the ucast receiver is blocked */
    public void monitorUcastReceiver() {
      UcastReceiver rcv = this.ucast_receiver;
      if (rcv != null) {
        rcv.monitorUcastReceiver();
      }
    }



    private BoundedList getLastPortsUsed() {
        if(last_ports_used == null)
            last_ports_used=new BoundedList(num_last_ports);
        return last_ports_used;
    }



    /* ----------------------- Receiving of MCAST UDP packets ------------------------ */

    public void run() {
        DatagramPacket  packet;
        byte            receive_buf[]=new byte[65535];
        int             offset, len, sender_port;
        byte[]          data;
        InetAddress     sender_addr;
        Address         sender;

        // moved out of loop to avoid excessive object creations (bela March 8 2001)
        packet=new DatagramPacket(receive_buf, receive_buf.length);

        for (;;)  { // GemStoneAddition remove coding anti-pattern
          if (mcast_recv_sock == null || mcast_recv_sock.isClosed()) break; // GemStoneAddition
          if (Thread.currentThread().isInterrupted()) break; // GemStoneAddition
            try {
                packet.setData(receive_buf, 0, receive_buf.length);
                if (Thread.currentThread().isInterrupted()) break; // GemStoneAddition -- for safety
                mcast_recv_sock.receive(packet);
                sender_addr=packet.getAddress();
                sender_port=packet.getPort();
                offset=packet.getOffset();
                len=packet.getLength();
                data=packet.getData();
                sender=new IpAddress(sender_addr, sender_port);
                ((IpAddress)sender).setBirthViewId(-1);
                stack.gfPeerFunctions.incMcastReadBytes(len);
                if(len > receive_buf.length) {
                    if(log.isErrorEnabled())
                        log.error("size of the received packet (" + len + ") is bigger than " +
                                  "allocated buffer (" + receive_buf.length + "): will not be able to handle packet. " +
                                  "Use the FRAG protocol and make its frag_size lower than " + receive_buf.length);
                }

                receive(mcast_addr, sender, data, offset, len);
            }
            catch(SocketException sock_ex) {
                 if(trace) log.trace("multicast socket is closed, exception=" + sock_ex);
                break;
            }
            catch(InterruptedIOException io_ex) { // thread was interrupted
              break; // GemStoneAddition -- exit loop and thread
            }
            catch (VirtualMachineError err) { // GemStoneAddition
              // If this ever returns, rethrow the error.  We're poisoned
              // now, so don't let this thread continue.
              throw err;
            }
            catch(Throwable ex) {
              if (!mcast_recv_sock.isClosed()) { // GemStoneAddition - no need to log exception if closing
                if (ex.getCause() != null) {
                  ex.fillInStackTrace(); // GemStoneAddition - no need to see the full stack twice
                }
                if(!Thread.currentThread().isInterrupted() && // GemStoneAddition - don't log shutdown errors
                   log.isErrorEnabled())
                    log.error(ExternalStrings.UDP_FAILURE_IN_MULTICAST_RECEIVE, ex);
                try { // GemStoneAddition
                Util.sleep(100); // so we don't get into 100% cpu spinning (should NEVER happen !)
                }
                catch (InterruptedException e) {
                  break; // exit loop and thread
                }
              }
            }
        }
        if(log.isDebugEnabled()) log.debug("multicast thread terminated");
    }

    @Override // GemStoneAddition  
    public String getInfo() {
        StringBuffer sb=new StringBuffer();
        sb.append("group_addr=").append(mcast_addr_name).append(':').append(mcast_port).append("\n");
        return sb.toString();
    }

    @Override // GemStoneAddition  
    public void sendToAllMembers(byte[] data, int offset, int length) throws Exception {
        if(ip_mcast && mcast_addr != null) {
            _send(mcast_addr, true, false, data, offset, length);
        }
        else {
            ArrayList mbrs=new ArrayList(members);
            IpAddress mbr;
            for(Iterator it=mbrs.iterator(); it.hasNext();) {
                mbr=(IpAddress)it.next();
                _send(mbr, false, false, data, offset, length);
            }
        }
    }

    @Override // GemStoneAddition  
    public void sendToSingleMember(Address dest, boolean isJoinResponse, byte[] data, int offset, int length) throws Exception {
        _send((IpAddress)dest, false, isJoinResponse, data, offset, length);
    }


    @Override // GemStoneAddition  
    public void postUnmarshalling(Message msg, Address dest, Address src, boolean multicast) {
      msg.setDest(dest);

      // GemStoneAddition -- fix for bugs 36611 and 46438; attempt to set a
      // canonical source address from this member's view
      Address sender = src;
      if (msg.getSrc() != null) {
        sender = msg.getSrc();
      }
      if (sender != null) {
        UnicastHeader hdr = msg.getHeader("UNICAST");
        if (hdr != null) {
          // For a JOIN request we need to use the address in the
          // GMS header since it will have more information
          // about the joiner
          GmsHeader ghdr = msg.getHeader("GMS");
          if (ghdr != null && ghdr.getType() == GmsHeader.JOIN_REQ) {
            msg.setSrc(ghdr.getMember());
            return;
          }
        }
        final Address canonical;
        synchronized(this.members) {
          // GemStoneAddition - canonical member IDs
          int idx = this.members.indexOf(sender);
          if (idx >= 0) {
            canonical = (Address)this.members.get(idx);
          } else {
            canonical = sender;
          }
          msg.setSrc(canonical);
        }
      }
    }

    @Override // GemStoneAddition  
    public void postUnmarshallingList(Message msg, Address dest, boolean multicast) {
        msg.setDest(dest);
    }

    // GemStoneAddition - take IpAddress instead of Inetaddr+port so we can
    // suspect the mbr if the NIC is down
    private void _send(IpAddress destAddr, boolean mcast, boolean isJoinResponse, byte[] data, int offset, int length) throws Exception {
        // GemStoneAddition - don't send if UDP has been closed
        if (closed)
          return;

        InetAddress dest = destAddr.getIpAddress();
        int port = destAddr.getPort();
        
        DatagramPacket packet=new DatagramPacket(data, offset, length, dest, port);
        try {
            if(mcast) {
                if(mcast_send_sock != null) {
                    long start = 0; // GemStoneAddition
                    if (stack.enableClockStats) {
                      start = stack.gfPeerFunctions.startMcastWrite(); // GemStoneAddition
                    }
                    mcast_send_sock.send(packet);
                    stack.gfPeerFunctions.endMcastWrite(start, length);
                }
                else {
                    if(mcast_send_sockets != null) {
                        MulticastSocket s;
                        for(int i=0; i < mcast_send_sockets.length; i++) {
                            s=mcast_send_sockets[i];
                            try {
                                // GemStoneAddition - statistics
                                long start = 0;
                                if (stack.enableClockStats) {
                                  start = stack.gfPeerFunctions.startMcastWrite(); // GemStoneAddition
                                }
                                s.send(packet);
                                stack.gfPeerFunctions.endMcastWrite(start, length);
                            }
                            // GemStoneAddition - we interrupt when closing, so don't log
                            // an "Exception" unless we're not closing
                            catch (java.io.InterruptedIOException ie) {
                              Thread.currentThread().interrupt();
                              throw new InterruptedException("Solaris-only: InterruptedIOException, we must be closing");
                              //if (!closed)
                              //  log.error("failed sending packet on socket " + s, ie); // GemStoneAddition - show reason
                            }
                            catch(Exception e) {
                                log.error(ExternalStrings.UDP_FAILED_SENDING_PACKET_ON_SOCKET__0, s, e); // GemStoneAddition - show reason
                            }
                        }
                    }
                    else {
                        throw new Exception("both mcast_send_sock and mcast_send_sockets are null");
                    }
                }
            }
            else {
                if(sock != null) {
                    // GemStoneAddition - statistics
                    long start = 0;
                    if (stack.enableClockStats) {
                      start = stack.gfPeerFunctions.startUcastWrite();
                    }
                    sock.send(packet);
                    if ((VERBOSE  || GemFireTracer.DEBUG) && isJoinResponse) {
                      log.getLogWriter().info(ExternalStrings.DEBUG,
                          "Sent packet of length " + length + " to " + dest + ":" + port);
                    }
                    stack.gfPeerFunctions.endUcastWrite(start, length);
                }
            }
        }
        // GemStoneAddition - we interrupt when closing, so don't throw
        // an "Exception" unless we're not closing
        catch (java.io.InterruptedIOException ie) {
          Thread.currentThread().interrupt();
          if (!closed)
            throw new Exception("dest=" + dest + ":" + port + " (" + length + " bytes)", ie);
        }
        catch (IOException ex) { // GemStoneAddition
          if (ex.getMessage().equalsIgnoreCase("operation not permitted")) {
            if (log.isTraceEnabled()) {
              log.trace("unable to send message to " + dest + ":" + port  + " (" + length + " bytes);  Operation was not permitted by datagram socket.");
            }
            passUp(new Event(Event.SUSPECT, new SuspectMember(local_addr, destAddr)));
          }
          else if (!stack.getChannel().closing()) { // GemStoneAddition - don't log if disconnecting
            Exception new_ex=new Exception("dest=" + dest + ":" + port + " (" + length + " bytes)", ex);
            throw new_ex;
          }
        }
        catch(Exception ex) {
            Exception new_ex=new Exception("dest=" + dest + ":" + port + " (" + length + " bytes)", ex);
            throw new_ex;
        }
    }


    /* ------------------------------------------------------------------------------- */



    /*------------------------------ Protocol interface ------------------------------ */

    @Override // GemStoneAddition  
    public String getName() {
        return "UDP";
    }


    // start GemStoneAddition
    @Override // GemStoneAddition  
    public int getProtocolEnum() {
      return com.gemstone.org.jgroups.stack.Protocol.enumUDP;
    }
    
    public DatagramSocket getMembershipSocket() {
      return this.sock;
    }
    // end GemStone addition




    /**
     * Creates the unicast and multicast sockets and starts the unicast and multicast receiver threads
     */
    @Override // GemStoneAddition  
    public void start() throws Exception {
        if(log.isDebugEnabled()) log.debug("creating sockets and starting threads");
        try {
          createSockets();
        } catch(Exception ex) {
          if (ex.getClass().getSimpleName().equals("SystemConnectException")) {
            throw ex;
          }
          String tmp="Problem creating sockets (bind_addr=" + bind_addr + ", mcast_addr=" + mcast_addr_name + ")";
          throw new Exception(tmp, ex);
        }
        super.start();
        startThreads();
    }


    @Override // GemStoneAddition  
    public void stop() {
        if(log.isDebugEnabled()) log.debug("closing sockets and stopping threads");
        stopThreads();  // will close sockets, closeSockets() is not really needed anymore, but...
        closeSockets(); // ... we'll leave it in there for now (doesn't do anything if already closed)
        super.stop();
    }





    /*--------------------------- End of Protocol interface -------------------------- */


    /* ------------------------------ Private Methods -------------------------------- */





    /**
     * Create UDP sender and receiver sockets. Currently there are 2 sockets
     * (sending and receiving). This is due to Linux's non-BSD compatibility
     * in the JDK port (see DESIGN).
     */
    private void createSockets() throws Exception {
        InetAddress tmp_addr;

        // GemStoneAddition - reuse the socket from a previous distributed system
        // for auto-reconnect
        DatagramSocket override = stack.gfPeerFunctions.getMembershipSocketForUDP();
        if (override != null && !override.isClosed()) {
          this.sock = override;
        }
        else {
          // bind_addr not set, try to assign one by default. This is needed on Windows

          // changed by bela Feb 12 2003: by default multicast sockets will be bound to all network interfaces

          // CHANGED *BACK* by bela March 13 2003: binding to all interfaces did not result in a correct
          // local_addr. As a matter of fact, comparison between e.g. 0.0.0.0:1234 (on hostA) and
          // 0.0.0.0:1.2.3.4 (on hostB) would fail !
          //        if(bind_addr == null) {
          //            InetAddress[] interfaces=InetAddress.getAllByName(InetAddress.getLocalHost().getHostAddress());
          //            if(interfaces != null && interfaces.length > 0)
          //                bind_addr=interfaces[0];
          //        }

          // GemStoneAddition - use Gemfire bind address, if present
          String bindAddress = System.getProperty("gemfire.jg-bind-address");
          // this code is for testing shunning of a member that reuses an address.
          // It is not a well-tested product feature
          int bp = Integer.getInteger("gemfire.jg-bind-port", 0);
          if (bp > 0) {
            bind_port = bp;
          }
          if (bindAddress != null && bindAddress.length() > 0) {
            bind_addr = InetAddress.getByName(bindAddress);
          }
          // GemStoneAddition - disabled to be compatible with other addr
          // discovery code in gemfire
          //if(bind_addr == null && !use_local_host) {
          //    bind_addr=Util.getFirstNonLoopbackAddress();
          //}
          if(bind_addr == null)
            bind_addr=stack.gfBasicFunctions.getLocalHost();

          if(bind_addr != null)
            if(log.isInfoEnabled()) log.info(ExternalStrings.UDP_DATAGRAM_SOCKETS_WILL_USE_INTERFACE__0, bind_addr.getHostAddress());


          // 2. Create socket for receiving unicast UDP packets. The address and port
          //    of this socket will be our local address (local_addr)
          if(bind_port > 0) {
            sock=createDatagramSocketWithBindPort();
          }
          else {
            sock=createEphemeralDatagramSocket();
          }
          if(tos > 0) {
            try {
              sock.setTrafficClass(tos);
            }
            catch(SocketException e) {
              // TOS is ignored by modern routers, so there's really no point in logging
              // an error message about failure to set it  (GemStoneAddition)
              //log.warn("traffic class of " + tos + " could not be set, will be ignored", e);
            }
          }
          if (sock != null) {
            // GemStoneAddition - Bug #51497: we need an soTimeout so the UDP reader thread won't
            // block indefinitely during a forced-disconnect
            try {
              if (log.isTraceEnabled()) {
                log.trace("setting SO_TIMEOUT on UDP unicast socket to 1000");
              }
              sock.setSoTimeout(1000);
            } catch (SocketException e) {
              log.debug("unable to set SO_TIMEOUT on UDP datagram port", e);
            }
          }

          if(sock == null)
            throw new Exception("UDP.createSocket(): sock is null");

        }

        local_addr=new IpAddress(sock.getLocalAddress(), sock.getLocalPort());
//        if (Locator.hasLocators()) {
//          ((IpAddress)local_addr).hasLocator(true);
//        }
        if(additional_data != null)
            ((IpAddress)local_addr).setAdditionalData(additional_data);

        // GemStoneAddition - member attributes
        stack.gfBasicFunctions.setDefaultGemFireAttributes((IpAddress)local_addr);

        // 3. Create socket for receiving IP multicast packets
        if(ip_mcast) {
            // 3a. Create mcast receiver socket
            tmp_addr=InetAddress.getByName(mcast_addr_name);
            // GemStoneAddition - variation on patch suggested by JGRP-777, see bug 40886
            if(can_bind_to_mcast_addr) {
              try {
                mcast_recv_sock = new MulticastSocket(new InetSocketAddress(tmp_addr, mcast_port));
              } catch(SocketException b) {
                //Handle OSes (ie Suse 10) that fail with above bind
                if (log.isDebugEnabled()) {
                  log.debug("unable to bind multicast socket to " + mcast_addr_name + ".  using null InetAddress instead");
                }
                can_bind_to_mcast_addr = false;
                mcast_recv_sock = new MulticastSocket(mcast_port);
              }
            } else {
              mcast_recv_sock = new MulticastSocket(mcast_port);
            }
            mcast_recv_sock.setTimeToLive(ip_ttl);
            mcast_addr=new IpAddress(tmp_addr, mcast_port);
            passUp(new Event(Event.SET_MCAST_ADDRESS, mcast_addr)); // GemStoneAddition

            if(receive_on_all_interfaces || (receive_interfaces != null && receive_interfaces.size() > 0)) {
                List interfaces;
                if(receive_interfaces != null)
                    interfaces=receive_interfaces;
                else
                    interfaces=Util.getAllAvailableInterfaces();
                bindToInterfaces(interfaces, mcast_recv_sock, mcast_addr.getIpAddress());
            }
            else {
                if(bind_addr != null) {
                    try {
                      mcast_recv_sock.setInterface(bind_addr);
                    } catch (java.net.SocketException e) {
                      stack.log.getLogWriter().warning(ExternalStrings.ONE_ARG, "Unable to set bind address on multicast socket: " + e.getMessage());
                    }
                }
                 mcast_recv_sock.joinGroup(tmp_addr);
            }

            // 3b. Create mcast sender socket
            if(send_on_all_interfaces || (send_interfaces != null && send_interfaces.size() > 0)) {
                List interfaces;
                NetworkInterface intf;
                if(send_interfaces != null)
                    interfaces=send_interfaces;
                else
                    interfaces=Util.getAllAvailableInterfaces();
                mcast_send_sockets=new MulticastSocket[interfaces.size()];
                int index=0;
                for(Iterator it=interfaces.iterator(); it.hasNext();) {
                    intf=(NetworkInterface)it.next();
                    mcast_send_sockets[index]=new MulticastSocket();
                    mcast_send_sockets[index].setNetworkInterface(intf);
                    mcast_send_sockets[index].setTimeToLive(ip_ttl);
                    if(tos > 0) {
                        try {
                            mcast_send_sockets[index].setTrafficClass(tos);
                        }
                        catch(SocketException e) {
                          // modern routers ignore traffic-class settings, so there's really
                          // no point in issuing a warning that it can't be set (GemStoneAddition)
                          //  log.warn("traffic class of " + tos + " could not be set, will be ignored", e);
                        }
                    }
                    index++;
                }
            }
            else {
                mcast_send_sock=new MulticastSocket();
                mcast_send_sock.setTimeToLive(ip_ttl);
                if(bind_addr != null) {
                  try {
                    mcast_send_sock.setInterface(bind_addr);
                  } catch (java.net.SocketException e) {
                    stack.log.getLogWriter().warning(ExternalStrings.ONE_ARG,
                        "Unable to set bind address on multicast socket: " +
                            e.getMessage());
                  }
                }

                if(tos > 0) {
                    try {
                        mcast_send_sock.setTrafficClass(tos); // high throughput
                    }
                    catch(SocketException e) {
                      // modern routers ignore traffic-class settings, so there's really
                      // no point in issuing a warning that it can't be set (GemStoneAddition)
                        // log.warn("traffic class of " + tos + " could not be set, will be ignored", e);
                    }
                }
            }
        }

        setBufferSizes();
        if(log.isInfoEnabled()) log.info(ExternalStrings.UDP_SOCKET_INFORMATIONN_0, dumpSocketInfo());
        closed = false; // GemStoneAddition
    }


//    private void bindToAllInterfaces(MulticastSocket s, InetAddress mcastAddr) throws IOException {
//        SocketAddress tmp_mcast_addr=new InetSocketAddress(mcastAddr, mcast_port);
//        Enumeration en=NetworkInterface.getNetworkInterfaces();
//        while(en.hasMoreElements()) {
//            NetworkInterface i=(NetworkInterface)en.nextElement();
//            for(Enumeration en2=i.getInetAddresses(); en2.hasMoreElements();) {
//                InetAddress addr=(InetAddress)en2.nextElement();
//                // if(addr.isLoopbackAddress())
//                // continue;
//                s.joinGroup(tmp_mcast_addr, i);
//                if(trace)
//                    log.trace("joined " + tmp_mcast_addr + " on interface " + i.getName() + " (" + addr + ")");
//                break;
//            }
//        }
//    }


    /**
     *
     * @param interfaces List<NetworkInterface>. Guaranteed to have no duplicates
     * @param s
     * @param mcastAddr
     * @throws IOException
     */
    private void bindToInterfaces(List interfaces, MulticastSocket s, InetAddress mcastAddr) throws IOException {
        SocketAddress tmp_mcast_addr=new InetSocketAddress(mcastAddr, mcast_port);
        for(Iterator it=interfaces.iterator(); it.hasNext();) {
            NetworkInterface i=(NetworkInterface)it.next();
            for(Enumeration en2=i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr=(InetAddress)en2.nextElement();
                s.joinGroup(tmp_mcast_addr, i);
                if(trace)
                    log.trace("joined " + tmp_mcast_addr + " on " + i.getName() + " (" + addr + ")");
                break;
            }
        }
    }



    /** Creates a DatagramSocket with a random port. Because in certain operating systems, ports are reused,
     * we keep a list of the n last used ports, and avoid port reuse */
    private DatagramSocket createEphemeralDatagramSocket() throws SocketException {
        DatagramSocket tmp = null;
        Random rand = new SecureRandom();
        // GemstoneAddition - a real membership port range
        int localPort = membership_port_range[0] +
                rand.nextInt(membership_port_range[1]-membership_port_range[0]+1);
//        if (localPort < 0) { // avoid wildcard ports
//          localPort = 1;
//        }
        int startingLocalPort = localPort;
        int lastPortInRange = membership_port_range[1];
        while(true) {
            if (localPort > lastPortInRange) {
                if (startingLocalPort != 0) {
                    localPort = membership_port_range[0];
                    lastPortInRange = startingLocalPort-1;
                    startingLocalPort = 0;
                } else {
                  stack.gfBasicFunctions.getSystemConnectException(ExternalStrings.UNABLE_TO_FIND_FREE_PORT.toLocalizedString());
                }
            }
            //tmp=new DatagramSocket(localPort, bind_addr); // first time localPort is 0
            if (localPort < 0) {
                localPort = 0; // GemStoneAddition
            }
            try {
                tmp = null; // GemStoneAddition
                tmp = new DatagramSocket(localPort, bind_addr); // first time localPort is 0
            }
            catch (java.net.SocketException b) { // GemStoneAddition
                if(Util.treatAsBindException(b)) {
                    localPort++; //GemStoneAddition
                } else {
                    //not an occurence of 40589, rethrow 
                    throw b;
                }
            }
            if (tmp != null) { // GemStoneAddition
                if(num_last_ports <= 0)
                    break;
                localPort=tmp.getLocalPort();
                if (getLastPortsUsed().contains(Integer.valueOf(localPort))) {
                  if (log.isDebugEnabled())
                    log.debug("local port " + localPort
                        + " already seen in this session; will try to get other port");
                  // if (tmp != null) (cannot be null)
                  // GemStoneAddition
                  try {
                    tmp.close();
                    localPort++;
                  } catch (VirtualMachineError err) { // GemStoneAddition
                    // If this ever returns, rethrow the error. We're poisoned
                    // now, so don't let this thread continue.
                    throw err;
                  } catch (Throwable e) {
                    localPort++;
                  }
                } else {
                    getLastPortsUsed().add(Integer.valueOf(localPort));
                    break;
                }
            }
        }
        return tmp;
    }




    /**
     * Creates a DatagramSocket when bind_port > 0. Attempts to allocate the socket with port == bind_port, and
     * increments until it finds a valid port, or until port_range has been exceeded
     * @return DatagramSocket The newly created socket
     * @throws Exception
     */
    private DatagramSocket createDatagramSocketWithBindPort() throws Exception {
        DatagramSocket tmp=null;
        // 27-6-2003 bgooren, find available port in range (start_port, start_port+port_range)
        int rcv_port=bind_port, max_port=bind_port + port_range;
        while(rcv_port <= max_port) {
            try {
                    tmp=new DatagramSocket(rcv_port, bind_addr);
                    break;
                }
            catch(SocketException bind_ex) {	// Cannot listen on this port
                rcv_port++;
            }
            catch(SecurityException sec_ex) { // Not allowed to listen on this port
                rcv_port++;
            }

            // Cannot listen at all, throw an Exception
            if(rcv_port >= max_port + 1) { // +1 due to the increment above
                throw new Exception("UDP.createSockets(): cannot create a socket on any port in range " +
                        bind_port + '-' + (bind_port + port_range));
            }
        }
        return tmp;
    }


    private String dumpSocketInfo() throws Exception {
        StringBuffer sb=new StringBuffer(128);
        sb.append("local_addr=").append(local_addr);
        sb.append(", mcast_addr=").append(mcast_addr);
        sb.append(", bind_addr=").append(bind_addr);
        sb.append(", ttl=").append(ip_ttl);

        if(sock != null) {
            sb.append("\nsock: bound to ");
            sb.append(sock.getLocalAddress().getHostAddress()).append(':').append(sock.getLocalPort());
            sb.append(", receive buffer size=").append(sock.getReceiveBufferSize());
            sb.append(", send buffer size=").append(sock.getSendBufferSize());
        }

        if(mcast_recv_sock != null) {
            sb.append("\nmcast_recv_sock: bound to ");
            sb.append(mcast_recv_sock.getInterface().getHostAddress()).append(':').append(mcast_recv_sock.getLocalPort());
            sb.append(", send buffer size=").append(mcast_recv_sock.getSendBufferSize());
            sb.append(", receive buffer size=").append(mcast_recv_sock.getReceiveBufferSize());
        }

         if(mcast_send_sock != null) {
            sb.append("\nmcast_send_sock: bound to ");
            sb.append(mcast_send_sock.getInterface().getHostAddress()).append(':').append(mcast_send_sock.getLocalPort());
            sb.append(", send buffer size=").append(mcast_send_sock.getSendBufferSize());
            sb.append(", receive buffer size=").append(mcast_send_sock.getReceiveBufferSize());
        }
        if(mcast_send_sockets != null) {
            sb.append("\n").append(mcast_send_sockets.length).append(" mcast send sockets:\n");
            MulticastSocket s;
            for(int i=0; i < mcast_send_sockets.length; i++) {
                s=mcast_send_sockets[i];
                sb.append(s.getInterface().getHostAddress()).append(':').append(s.getLocalPort());
                sb.append(", send buffer size=").append(s.getSendBufferSize());
                sb.append(", receive buffer size=").append(s.getReceiveBufferSize()).append("\n");
            }
        }
        return sb.toString();
    }


    void setBufferSizes() {
        if(sock != null)
            setBufferSize(sock, ucast_send_buf_size, ucast_recv_buf_size, false); // GemStoneAddition - mcast flag

        if(mcast_recv_sock != null)
            setBufferSize(mcast_recv_sock, mcast_send_buf_size, mcast_recv_buf_size, true); // GemStoneAddition - mcast flag

        if(mcast_send_sock != null)
            setBufferSize(mcast_send_sock, mcast_send_buf_size, mcast_recv_buf_size, true); // GemStoneAddition - mcast flag

        if(mcast_send_sockets != null) {
            for(int i=0; i < mcast_send_sockets.length; i++) {
                setBufferSize(mcast_send_sockets[i], mcast_send_buf_size, mcast_recv_buf_size, true); // GemStoneAddition - mcast flag
            }
        }
    }

    private void setBufferSize(DatagramSocket sock, int send_buf_size, int recv_buf_size, boolean ismcast) {
        String socktype = ismcast? "multicast" : "unicast";  // GemStoneAddition
        try {
            sock.setSendBufferSize(send_buf_size);
            int real=sock.getSendBufferSize();
            // GemStoneAddition - warn if we don't get what we want
            if (real != send_buf_size) {
              int def = ismcast? 65535 : 65535; 
                  // DistributionConfig.DEFAULT_MCAST_SEND_BUFFER_SIZE : DistributionConfig.DEFAULT_UDP_SEND_BUFFER_SIZE;
              StringId msg = ExternalStrings.UDP_REQUESTED_0_1_OF_2_BUT_GOT_3;
              Object[] msgArgs = new Object[] {socktype, "send buffer size", Integer.valueOf(send_buf_size), Integer.valueOf(real)};
              // for user-requested sizes, log a warning.  Otherwise log as info
              if (send_buf_size != def) {
                log.getLogWriter().warning(msg, msgArgs);
              }
              else {
                log.getLogWriter().info(msg, msgArgs);
              }
            }
        }
        catch (VirtualMachineError err) { // GemStoneAddition
          // If this ever returns, rethrow the error.  We're poisoned
          // now, so don't let this thread continue.
          throw err;
        }
        catch(Throwable ex) {
            if(warn) log.warn("failed setting send buffer size of " + send_buf_size + " in " + sock + ": " + ex);
        }

        try {
            sock.setReceiveBufferSize(recv_buf_size);
            int real=sock.getReceiveBufferSize();
            // GemStoneAddition - warn if we don't get what we want
            if (real != recv_buf_size) {
              int def = ismcast ? 1048576 : 1048576;
                // DistributionConfig.DEFAULT_MCAST_RECV_BUFFER_SIZE : DistributionConfig.DEFAULT_UDP_RECV_BUFFER_SIZE;
              StringId msg = ExternalStrings.UDP_REQUESTED_0_1_OF_2_BUT_GOT_3;
              Object[] msgArgs = new Object[] {socktype, "receive buffer size", Integer.valueOf(recv_buf_size), Integer.valueOf(real)};
              
              // for user-requested sizes, log a warning.  Otherwise log as info
              if (recv_buf_size != def) {
                log.getLogWriter().warning(msg, msgArgs);
              }
              else {
                log.getLogWriter().info(msg, msgArgs);
              }
            }
        }
        catch (VirtualMachineError err) { // GemStoneAddition
          // If this ever returns, rethrow the error.  We're poisoned
          // now, so don't let this thread continue.
          throw err;
        }
        catch(Throwable ex) {
            if(warn) log.warn("failed setting receive buffer size of " + recv_buf_size + " in " + sock + ": " + ex);
        }
    }


    /**
     * Closed UDP unicast and multicast sockets
     */
    synchronized void closeSockets() {  // GemStoneAddition - synchronization
        // 1. Close multicast socket
        closeMulticastSocket();

        // 2. Close socket
        closeSocket();
    }


    void closeMulticastSocket() {
        if(mcast_recv_sock != null) {
            try {
                if(mcast_addr != null) {
                    mcast_recv_sock.leaveGroup(mcast_addr.getIpAddress());
                }
                mcast_recv_sock.close(); // this will cause the mcast receiver thread to break out of its loop
                //mcast_recv_sock=null;
                if(log.isDebugEnabled()) log.debug("multicast receive socket closed");
            }
            catch(IOException ex) {
            }
            mcast_addr=null;
        }

        if(mcast_send_sock != null) {
            mcast_send_sock.close();
            //mcast_send_sock=null;
            if(log.isDebugEnabled()) log.debug("multicast send socket closed");
        }
        if(mcast_send_sockets != null) {
            MulticastSocket s;
            for(int i=0; i < mcast_send_sockets.length; i++) {
                s=mcast_send_sockets[i];
                s.close();
                if(log.isDebugEnabled()) log.debug("multicast send socket " + s + " closed");
            }
            mcast_send_sockets=null;
        }
    }


    protected/*GemStoneAddition*/ void closeSocket() {
        if(sock != null) {
          JChannel channel = (JChannel)stack.getChannel();
          Exception closeException = channel.getCloseException();
          boolean isForcedDisconnect = closeException != null && 
              closeException.getClass().getSimpleName().equals("ForcedDisconnectException"); 
          if (stack.gfPeerFunctions.getDisableAutoReconnect()
              || (!isForcedDisconnect && !stack.gfPeerFunctions.isReconnectingDS()) ) {
            /* if a FDE has occurred we want to keep the socket open.
             * Otherwise we want to keep the socket open if the distributed
             * system is trying to reconnect and has the socket cached for
             * quorum checks.
             */
            if (log.isTraceEnabled()) {
              log.trace("UDP is closing its socket.  Channel close exception = "+((JChannel)stack.getChannel()).getCloseException()
                  +" isReconnecting="+stack.gfPeerFunctions.isReconnectingDS()
                  +" disableAutoReconnect="+stack.gfPeerFunctions.getDisableAutoReconnect()
                  +" port="+sock.getLocalPort()
                  +" isClosed="+sock.isClosed());
            }
            sock.close();
            //sock=null;
            if(log.isDebugEnabled()) log.debug("socket closed");
          } else {
            if (log.getLogWriter().fineEnabled()) {
              log.getLogWriter().fine("UDP is not closing its socket.  Channel close exception = "
                  + ((JChannel)stack.getChannel()).getCloseException() + " isReconnecting="
                  + stack.gfPeerFunctions.isReconnectingDS() + "  disableAutoReconnect="+
                  stack.gfPeerFunctions.getDisableAutoReconnect() + " port=" + sock.getLocalPort() + " isClosed="+sock.isClosed());
            }
          }
        }
    }


    /**
     * Just ensure that this class gets loaded.
     */
    public static void loadEmergencyClasses() {
      // nothing more needed
    }
    
    /**
     * Closes the datagram socket, any multicast receive sockets,
     * and interrupts their threads.
     */
    @Override // GemStoneAddition  
    public void emergencyClose() {
      closeSocket();

      MulticastSocket ms = mcast_recv_sock;
      if (ms != null) {
        ms.close();
      }
      ms = mcast_send_sock;
      if (ms != null) {
        ms.close();
      }
      if (mcast_send_sockets != null) {
        for (int i = 0; i < mcast_send_sockets.length; i ++) {
          ms = mcast_send_sockets[i];
          if (ms != null) {
            ms.close();
          }
        }
      }
      
      Thread thr = mcast_receiver;
      if (thr != null) {
        thr.interrupt();
      }
      UcastReceiver ur = ucast_receiver;
      if (ur != null) {
        thr = ur.thread;
        if (thr != null) {
          thr.interrupt();
          }
      }
    }


    /**
     * Starts the unicast and multicast receiver threads
     */
    void startThreads() throws Exception {
        if(ucast_receiver == null) {
            //start the listener thread of the ucast_recv_sock
            ucast_receiver=new UcastReceiver();
            ucast_receiver.start();
        }

        if(ip_mcast) {
          synchronized (this) { // GemStoneAddition
            if(mcast_receiver != null) {
                if(mcast_receiver.isAlive()) {
                    if(log.isDebugEnabled()) log.debug("did not create new multicastreceiver thread as existing " +
                                                       "multicast receiver thread is still running");
                }
                else
                    mcast_receiver=null; // will be created just below...
            }

            if(mcast_receiver == null) {
                mcast_receiver=new Thread(GemFireTracer.GROUP, this, "UDP mcast receiver");
                mcast_receiver.setPriority(Thread.MAX_PRIORITY);
                mcast_receiver.setDaemon(true);
                mcast_receiver.start();
            }
          }
        }
    }


    /**
     * Stops unicast and multicast receiver threads
     */
    void stopThreads() {
        Thread tmp;

        // 1. Stop the multicast receiver thread
        synchronized (this) { // GemStoneAddition
        if(mcast_receiver != null) {
            if(mcast_receiver.isAlive()) {
                tmp=mcast_receiver;
                mcast_receiver=null;
                // GemStoneAddition -- interrupt before closing the socket, so
                // that the thread knows it's being shutdown.
                tmp.interrupt();
                closeMulticastSocket();  // will cause the multicast thread to terminate
                try {
                    tmp.join(100);
                }
                catch(InterruptedException e) {
                  Thread.currentThread().interrupt(); // GemStoneAddition
                  // propagate to caller
                }
                tmp=null;
            }
            mcast_receiver=null;
        }
        }

        // 2. Stop the unicast receiver thread
        if(ucast_receiver != null) {
            ucast_receiver.stop();
            ucast_receiver=null;
        }
    }



    @Override // GemStoneAddition  
    protected void handleConfigEvent(HashMap map) {
        super.handleConfigEvent(map);
        if(map == null) return;
        if(map.containsKey("send_buf_size")) {
            mcast_send_buf_size=((Integer)map.get("send_buf_size")).intValue();
            ucast_send_buf_size=mcast_send_buf_size;
        }
        if(map.containsKey("recv_buf_size")) {
            mcast_recv_buf_size=((Integer)map.get("recv_buf_size")).intValue();
            ucast_recv_buf_size=mcast_recv_buf_size;
        }
        setBufferSizes();
    }


//    private void nullAddresses(Message msg, IpAddress dest, IpAddress src) {
//        if(src != null) {
//            if(null_src_addresses)
//                msg.setSrc(new IpAddress(src.getPort(), false));  // null the host part, leave the port
//            if(src.getAdditionalData() != null)
//                ((IpAddress)msg.getSrc()).setAdditionalData(src.getAdditionalData());
//        }
//        else if(dest != null && !dest.isMulticastAddress()) { // unicast
//            msg.setSrc(null);
//        }
//    }

  //  private void setAddresses(Message msg, Address dest) {
   //     msg.setDest(dest);
//
//        // set the source address if not set
//        IpAddress src_addr=(IpAddress)msg.getSrc();
//        if(src_addr == null) {
//            msg.setSrc(sender);
//        }
//        else {
//            byte[] tmp_additional_data=src_addr.getAdditionalData();
//            if(src_addr.getIpAddress() == null) {
//                IpAddress tmp=new IpAddress(sender.getIpAddress(), src_addr.getPort());
//                msg.setSrc(tmp);
//            }
//            if(tmp_additional_data != null)
//                ((IpAddress)msg.getSrc()).setAdditionalData(tmp_additional_data);
//        }
 //   }



    /* ----------------------------- End of Private Methods ---------------------------------------- */

    /* ----------------------------- Inner Classes ---------------------------------------- */




    public class UcastReceiver implements Runnable {
//        boolean running=true; GemStoneAddition
        Thread thread=null; // GemStoneAddition -- accesses synchronized via this


        synchronized /* GemStoneAddition */ public void start() {
            if(thread == null) {
                thread=new Thread(GemFireTracer.GROUP, this, "UDP ucast receiver"); // GemStoneAddition - make name uniform with mcast thread
                thread.setDaemon(true);
                thread.setPriority(Thread.MAX_PRIORITY); // GemStoneAddition - mandatory or FD fails easily
//                running=true; GemStoneAddition
                thread.start();
            }
        }
        
        void monitorUcastReceiver() {
          final Thread ucastThread = thread;
          Thread monThread = new Thread("stack dumper") {
            public void run() {
              try {
                while (ucastThread.isAlive()) {
                  sleep(1000);
                  logStackTrace(ucastThread);
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          };
          monThread.setDaemon(true);
          monThread.start();
        }
        
        private void logStackTrace(Thread t) {
          StringWriter sw = new StringWriter(500);
          int i = 0;
          StackTraceElement[] stackTrace = t.getStackTrace();
          for (; i < stackTrace.length && i < 500; i++) {
            StackTraceElement ste = stackTrace[i];
            sw.append("\tat " + ste.toString());
            sw.append('\n');
          }
          log.getLogWriter().info(ExternalStrings.ONE_ARG, sw.toString());
        }


        synchronized /* GemStoneAddition */ public void stop() {
            Thread tmp;
            if(thread != null && thread.isAlive()) {
//                running=false; GemStoneAddition
                tmp=thread;
                thread=null;
                // GemStoneAddition -- interrupt thread before closing socket,
                // so that it will know it's being shut down.
                tmp.interrupt();
                closeSocket(); // this will cause the thread to break out of its loop
                tmp=null;
            }
            thread=null;
        }


        public void run() {
            DatagramPacket  packet;
            byte            receive_buf[]=new byte[65535];
            int             offset, len;
            byte[]          data;
            InetAddress     sender_addr;
            int             sender_port;
            Address         sender;

            // moved out of loop to avoid excessive object creations (bela March 8 2001)
            packet=new DatagramPacket(receive_buf, receive_buf.length);

            for (;;) { // GemStoneAddition remove coding anti-pattern
//              if (sock.isClosed()) break; // GemStoneAddition ...but just let the receive() fail, it's cheaper.
              if (Thread.currentThread().isInterrupted()) break; // GemStoneAddition
              synchronized(this) {
                if (this.thread == null) {  // GemStoneAddition
                  break;
                }
              }
                try {
                    packet.setData(receive_buf, 0, receive_buf.length);
                    sock.receive(packet);
                    sender_addr=packet.getAddress();
                    sender_port=packet.getPort();
                    offset=packet.getOffset();
                    len=packet.getLength();
                    data=packet.getData();
                    sender=new IpAddress(sender_addr, sender_port);
                    // the default IpAddress will have the wrong viewId and processId
                    // so we need to reset these to defaults
                    ((IpAddress)sender).setBirthViewId(-1);
                    ((IpAddress)sender).setProcessId(0);
                    stack.gfPeerFunctions.incUcastReadBytes(len);

                    if(len > receive_buf.length) {
                        if(log.isErrorEnabled())
                            log.error("size of the received packet (" + len + ") is bigger than allocated buffer (" +
                                      receive_buf.length + "): will not be able to handle packet. " +
                                      "Use the FRAG protocol and make its frag_size lower than " + receive_buf.length);
                    }
                    receive(local_addr, sender, data, offset, len);
                }
                catch (SocketTimeoutException e) { // GemStoneAddition - we set soTimeout on the socket
                }
                catch(SocketException sock_ex) {
                    if(log.isDebugEnabled()) log.debug("unicast receiver socket is closed, exception=" + sock_ex);
                    break;
                }
                catch(InterruptedIOException io_ex) { // thread was interrupted
                  break; // GemStoneAddition -- exit loop and thread
                }
                catch (VirtualMachineError err) { // GemStoneAddition
                  // If this ever returns, rethrow the error.  We're poisoned
                  // now, so don't let this thread continue.
                  throw err;
                }
                catch(Throwable ex) {
                  if (!sock.isClosed()) { // GemStoneAddition - no error logs if closing
                    if (ex.getCause() != null) {
                      ex.fillInStackTrace(); // GemStoneAddition - no need to see the full stack twice
                    }
                    if(!Thread.currentThread().isInterrupted() // GemStoneAddition - don't log shutdown errors
                       && log.isErrorEnabled())
                        log.error(ExternalStrings.UDP__0__FAILED_RECEIVING_UNICAST_PACKET, local_addr, ex);
                    try { // GemStoneAddition
                    Util.sleep(100); // so we don't get into 100% cpu spinning (should NEVER happen !)
                    }
                    catch (InterruptedException e) {
                      break; // exit loop and thread
                    }
                  }
                }
            }
            if(log.isDebugEnabled()) log.debug("unicast receiver thread terminated");
        }
    }

}
