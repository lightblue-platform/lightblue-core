#!/bin/bash

echo "after_success script"

if [ "$BRANCH" == "master" ]; then
     if [ "$JDK_VERSION" == "oraclejdk8" ] && [ "$PULL_REQUEST" == "false" ]; then
         echo "DEPLOY MASTER BUILD"
         echo "Current directory is $(pwd)"
         mvn clean deploy -DskipTests;
     fi
fi

