#!/bin/sh

CLASSPATH=target/*-jar-with-dependencies.jar
exec java -cp $CLASSPATH org.walkmod.kafka.webhook.ApplicationMain
