#!/bin/bash
pid=`ps aux | grep "com.xx_dev.apn.proxy.ApnProxyServerLauncher" | grep java | awk '{print $2}' | sort | head -1`

if [ -n "$pid" ]; then
    echo "Stop old apn-proxy server: $pid"
    kill $pid
else
    echo "No old apn-proxy server"
fi

sleep 2

for jar in `ls lib/*.jar`
do
    jars="$jars:""$jar"
done
java $JAVA_OPTS -cp $jars com.xx_dev.apn.proxy.ApnProxyServerLauncher 1>/dev/null 2>&1 &