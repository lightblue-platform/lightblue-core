#!/bin/sh

# kill any previous port forward
pkill -9 -f "rhc port-forward sonar [-]n lightblue"

# forward ports from openshift (assumes current server is openshift online and you're a member of the lightblue namespace)
rhc port-forward sonar -n lightblue &

# wait a bit for port forwarding to fire up
sleep 10

# build and publish
mvn clean install -Dmaven.test.failure.ignore=true  
mvn -e -B sonar:sonar

# cleanup port forwarding
pkill -9 -f "rhc port-forward sonar [-]n lightblue"
