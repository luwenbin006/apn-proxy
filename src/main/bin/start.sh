#!/bin/bash
if [ -f pid ]
then
	pid=`cat pid`
	kill $pid
fi
for jar in `ls lib/*.jar`
do
	jars="$jars:""$jar"
done
java -Xmx128m -cp $jars com.xx_dev.apn.proxy.ApnProxyServerLauncher %*  1>/dev/null 2>&1 & echo $! > pid