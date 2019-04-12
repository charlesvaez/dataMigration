#!/bin/bash
docker run -p 8088:8088 -v /logs:/logs --name redis-data-migration -d redis-data-migration -Xms1024m -Xmx1024m -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=256M -XX:+UseCompressedOops -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintGCTimeStamps -XX:+PrintClassHistogram -XX:+UseFastAccessorMethods -XX:+AggressiveOpts -Xloggc:/logs/GC.log -XX:ErrorFile=/logs/hs_error_%p.log -XX:+DisableExplicitGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/jvmdump -XX:+UseBiasedLocking -XX:+PrintCommandLineFlags


