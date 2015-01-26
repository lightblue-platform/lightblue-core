#!/usr/bin/env sh

echo "onSucess script"

if [ $TRAVIS_BRANCH == "master" ] && [ $TRAVIS_JDK_VERSION == "openjdk7" ]; then
    echo "DEPLOY MASTER TRAVIS BUILD"
    echo "Current directory is $(pwd)"
    mvn clean deploy -DskipTests;
fi
