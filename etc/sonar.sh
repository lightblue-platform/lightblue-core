#!/bin/sh

# kill any previous port forward and the previous mark file  about its status
pkill -9 -f "rhc port-forward sonar [-]n lightblue"
rm -f rhc-port-forward-status

# forward ports from openshift (assumes current server is openshift online and you're a member of the lightblue namespace). A new mark file will eb created if the return value of the rhc command is different than 0
rhc port-forward sonar -n lightblue || echo $? > rhc-port-forward-status  &

# wait a bit for port forwarding to fire up
sleep 10

# if the file exists, log the error and exit 1
if [ -f "rhc-port-forward-status" ]
then
    echo "Error on rhc port-forward. Error code:"
    cat rhc-port-forward-status 
    rm -f rhc-port-forward-status
    # rhc process should dead, so it would not need  pkill -9 -f "rhc port-forward sonar [-]n lightblue"
    exit 1
fi

# build and publish
mvn clean install -Dmaven.test.failure.ignore=true  
mvn -e -B sonar:sonar

# cleanup port forwarding & mark file
pkill -9 -f "rhc port-forward sonar [-]n lightblue"
rm -f rhc-port-forward-status
