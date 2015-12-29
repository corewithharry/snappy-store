/** Notice of modification as required by the LGPL
 *  This file was modified by Gemstone Systems Inc. on
 *  $Date$
 **/
// $Id: LogicalAddress.java,v 1.9 2005/07/17 11:34:20 chrislott Exp $

package com.gemstone.org.jgroups.stack;


import com.gemstone.org.jgroups.Address;
import com.gemstone.org.jgroups.util.Util;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
 * Logical address that spans the lifetime of a member. Assigned at member (JVM) startup, and
 * retained until member is shutdown. Note that the address does <em>not</em> change on
 * disconnect-connect sequences. For example, when a member is shunned and subsequently
 * readmitted to the group, the member's address (LogicalAddress) remains the same.<br/>
 * An instance of LogicalAddress is generated by the transport protocol. Currently, only
 * UDP_NIO generates LogicalAddresses.<br/>
 * Note that host, timestamp and id are supposed to make LogicalAddress as unique as possible.
 * However, there is a remote chance that 2 instances started on the same machine create their
 * address at exactly the same time, resulting in identical addresses (leading to problems).
 * In the future, I will try to make this totally unique, by for example using the PID of the current
 * process (once available though the JDK, or by locking on a common resource (e.g. /dev/random)
 * to serialize creation. However, as for now, chances are you will never experience this problem.
 * @author Bela Ban, Dec 23 2003
 */
public class LogicalAddress implements Address {
    static   int count=1;
    protected String   host=null;
    protected long     timestamp=0;
    protected int      id=0;
    protected boolean  multicast_addr=false;

    // GemStoneAddition
    public boolean preferredForCoordinator() {
      return true;
    }
    public boolean splitBrainEnabled() {
      return false;
    }
    
    @Override
    public int getBirthViewId() {
      return -1;
    }
    
    @Override
    public short getVersionOrdinal() {
      return -1;
    }

    /** Address of the primary physical address. This is set to the sender when a message is received.
     * If this field is set, we will send unicast messages only to this address, not to all addresses listed
     * in physical_addrs; this reduces the number of msgs we have to send.<br/>
     * Note that this field is not shipped across the wire.
     */
    transient SocketAddress primary_physical_addr=null;

    /** List<SocketAddress> of physical addresses */
    protected ArrayList physical_addrs=null;

    /** To tack on some additional data */
    byte[] additional_data=null;



    // Used only by Externalization
    public LogicalAddress() {
    }


    /** Use this constructor to create an instance, not the null-constructor */
    public LogicalAddress(String host_name, List physical_addrs) {
        init(host_name, physical_addrs);
    }



    protected void init(String host_name, List physical_addrs) {
        if(host_name != null) {
            this.host=host_name;
        }
        else {
            try {
                host=InetAddress.getLocalHost().getHostName();
            }
            catch(Exception e) {
                host="localhost";
            }
        }

        timestamp=System.currentTimeMillis();

        synchronized(LogicalAddress.class) {
            id=count++;
        }

        if(physical_addrs != null) {
            this.physical_addrs=new ArrayList(physical_addrs);
        }
    }


    public String getHost() {
        return host;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getId() {
        return id;
    }

    public SocketAddress getPrimaryPhysicalAddress() {
        return primary_physical_addr;
    }

    public void setPrimaryPhysicalAddress(SocketAddress primary_physical_addr) {
        this.primary_physical_addr=primary_physical_addr;
    }

    /**
     * Returns a <em>copy</em> of the list of physical addresses. Reason for the copy is that the list is not supposed
     * to be modified (should be immutable).
     * @return List of physical addresses (return value maybe null)
     */
    public ArrayList getPhysicalAddresses() {
        return physical_addrs != null? (ArrayList)physical_addrs.clone() : null;
    }

    /**
     * For internal use only. Don't use this method!
     * @param addr
     */
    public void addPhysicalAddress(SocketAddress addr) {
        if(addr != null) {
            if(physical_addrs == null)
                physical_addrs=new ArrayList();
            if(!physical_addrs.contains(addr))
                physical_addrs.add(addr);
        }
    }

    /**
     * For internal use only. Don't use this method !
     * @param addr
     */
    public void removePhysicalAddress(SocketAddress addr) {
        if(addr != null && physical_addrs != null)
            physical_addrs.remove(addr);
    }

    /**
     * For internal use only. Don't use this method !
     */
    public void removeAllPhysicalAddresses() {
        if(physical_addrs != null)
            physical_addrs.clear();
    }

    public boolean isMulticastAddress() {
        return false; // LogicalAddresses can never be multicast
    }

    public int size(short version) {
        return 22;
    }

    /**
     * Returns the additional_data.
     * @return byte[]
     */
    public byte[] getAdditionalData() {
        return additional_data;
    }

    /**
     * Sets the additional_data.
     * @param additional_data The additional_data to set
     */
    public void setAdditionalData(byte[] additional_data) {
        this.additional_data = additional_data;
    }


    /**
     * Establishes an order between 2 addresses. Assumes other contains non-null IpAddress.
     * Excludes channel_name from comparison.
     * @return 0 for equality, value less than 0 if smaller, greater than 0 if greater.
     */
    public int compare(LogicalAddress other) {
        return compareTo(other);
    }


    /**
     * implements the java.lang.Comparable interface
     * @see Comparable
     * @param o - the Object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than,
     *         equal to, or greater than the specified object.
     * @exception ClassCastException - if the specified object's type prevents it
     *            from being compared to this Object.
     */
    public int compareTo(Object o) {
        int   rc;

        if ((o == null) || !(o instanceof LogicalAddress))
            throw new ClassCastException("LogicalAddress.compareTo(): comparison between different classes");
        LogicalAddress other = (LogicalAddress) o;

        rc=this.host.compareTo(other.host);
        if(rc != 0) return rc;
        if(this.timestamp != other.timestamp)
            return this.timestamp < other.timestamp? -1 : 1;
        if(this.id != other.id)
            return this.id < other.id? -1 : 1;
        return 0;
    }



    @Override // GemStoneAddition
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if (!(obj instanceof LogicalAddress)) return false; // GemStoneAddition
        return compareTo(obj) == 0 ? true : false;
    }




    @Override // GemStoneAddition
    public int hashCode() {
        int retval=(int)(host.hashCode() + timestamp + id);
        return retval;
    }




    @Override // GemStoneAddition
    public String toString() {
        return toString(false);
    }


    public String toString(boolean print_details) {
        StringBuffer sb=new StringBuffer();

        sb.append(host);
        sb.append(':').append(id);
        if(print_details) {
            sb.append(" (created ").append(new Date(timestamp)).append(')');
            if(physical_addrs != null)
                sb.append("\nphysical addrs: ").append(physical_addrs);
        }
        if(additional_data != null)
            sb.append(" (additional data: ").append(additional_data.length).append(" bytes)");
        return sb.toString();
    }



    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(host);
        out.writeLong(timestamp);
        out.writeInt(id);

        if(physical_addrs != null) {
            out.writeInt(physical_addrs.size());
            out.writeObject(physical_addrs);
        }
        else
            out.writeInt(0);

        if(additional_data != null) {
            out.writeInt(additional_data.length);
            out.write(additional_data, 0, additional_data.length);
        }
        else
            out.writeInt(0);
    }




    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int len;

        host=(String)in.readObject();
        timestamp=in.readLong();
        id=in.readInt();

        len=in.readInt();
        if(len > 0) {
            physical_addrs=(ArrayList)in.readObject();
        }

        len=in.readInt();
        if(len > 0) {
            additional_data=new byte[len];
            in.readFully(additional_data, 0, additional_data.length);        
        }
    }



    public void writeTo(DataOutputStream out) throws IOException {
        Util.writeString(host, out);
        out.writeLong(timestamp);
        out.writeInt(id);
        out.writeBoolean(multicast_addr);
        ObjectOutputStream oos=new ObjectOutputStream(out);
        oos.writeObject(physical_addrs);
        oos.close();
        Util.writeByteBuffer(additional_data, out);
    }

    public void readFrom(DataInputStream in) throws IOException, IllegalAccessException, InstantiationException {
        host=Util.readString(in);
        timestamp=in.readLong();
        id=in.readInt();
        multicast_addr=in.readBoolean();
        ObjectInputStream ois=new ObjectInputStream(in);
        try {
            physical_addrs=(ArrayList)ois.readObject();
        }
        catch(ClassNotFoundException e) {
        }
        additional_data=Util.readByteBuffer(in);
    }

    @Override // GemStoneAddition
    public Object clone() throws CloneNotSupportedException {
        LogicalAddress ret=new LogicalAddress();
        ret.host=host;
        ret.timestamp=timestamp;
        ret.id=id;
        ret.multicast_addr=multicast_addr;
        ret.additional_data=additional_data;
        ret.primary_physical_addr=primary_physical_addr;
        if(physical_addrs != null)
            ret.physical_addrs=(ArrayList)physical_addrs.clone();
        return ret;
    }

    public LogicalAddress copy() {
        try {
            return (LogicalAddress)clone();
        }
        catch(CloneNotSupportedException e) {
            return null;
        }
    }


}
