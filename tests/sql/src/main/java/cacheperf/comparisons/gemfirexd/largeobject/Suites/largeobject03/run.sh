#!/bin/sh

runtest() {
  export GEMFIRE_BUILD=$1
  export GEMFIRE=$GEMFIRE_BUILD/product
  export LD_LIBRARY_PATH=$GEMFIRE/../hidden/lib:$GEMFIRE/lib
  export JTESTS=$GEMFIRE/../tests/classes
  export CLASSPATH=$GEMFIRE/lib/gemfire.jar:$JTESTS
  export JAVA_HOME=/export/gcm/where/jdk/1.6.0_17/x86_64.linux
  echo "Running $2 with $3..."
  echo ""
  $JAVA_HOME/bin/java -server \
    -classpath $CLASSPATH:$GEMFIRE/../product-gfxd/lib/gemfirexd.jar -DGEMFIRE=$GEMFIRE -DJTESTS=$JTESTS \
    -DprovideRegressionSummary=false -DnukeHungTest=true -DmoveRemoteDirs=true \
    -DnumTimesToRun=1 -DtestFileName=$2 -DlocalConf=$3 \
    batterytest.BatteryTest
  echo "Saving test results to $4..."
  echo ""
  mkdir $4
  /bin/mv *-*-* $4
  /bin/mv batterytest.log batterytest.bt oneliner.txt $4
  /bin/cp my.cnf $4
  /bin/cp $2 $3 $4
}

#-------------------------------------------------------------------------------

if [ -z "$1" ]
then
  echo "No gemfire build was specified."
  exit 0
fi

runtest $1 p2p.gfe.bt           p2p.local.conf        p2p.gfe
runtest $1 p2p.gfxd.bt          p2p.local.conf        p2p.gfxd
runtest $1 peerClient.gfe.bt    peerClient.local.conf peerClient.gfe
runtest $1 peerClient.gfxd.bt   peerClient.local.conf peerClient.gfxd
runtest $1 thinClient.gfe.bt    thinClient.local.conf thinClient.gfe
runtest $1 thinClient.gfxd.bt   thinClient.local.conf thinClient.gfxd
./startup.sh
runtest $1 thinClient.mysql.bt  thinClient.local.conf thinClient.mysql
./shutdown.sh
