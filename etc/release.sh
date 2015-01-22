#!/bin/sh

# https://github.com/lightblue-platform/lightblue-docs-developer/issues/6

RELEASE_VERSION=$1
DEVEL_VERSION=$2

if [ $1"x" == "x" ] || [ $2"x" == "x" ]; then
    echo "Usage: ./release.sh <release version> <new snapshot version>"
    echo "Example: ./release 1.1.0 1.2.0-SNAPSHOT"
    exit 1
fi

# prepare and verify state
git fetch --all
rm -rf ~/.m2/repository/com/redhat/lightblue/

BRANCH=`git branch | grep ^* | awk '{print $2}'`

if [ $BRANCH != "master" ]; then
    read -p "Current branch is '${BRANCH}', not 'master'.  Do you wish to continue? (y/N) "
    if [ "$REPLY" != "y"]; then
        exit 1
    fi
fi

# check that local branch is equal to upstream master (assumes remote of origin)
MERGE_BASE=`git merge-base HEAD origin/master`
HEAD_HASH=`git rev-parse HEAD`

if [ $MERGE_BASE != $HEAD_HASH ]; then
    echo "Local branch is not in sync with origin/master.  Fix and run this script again."
    exit 1
fi

# update to non-snapshot versions of lightblue dependencies and commit
mvn versions:update-properties -DallowSnapshots=false
git commit -a -m "Updated versions to non snapshot"

# prepare for release (note, this will warn if any snapshot dependencies still exist and allow for fixing)
mvn release:prepare -P release \
                    -DpushChanges=false \
                    -DreleaseVersion=$RELEASE_VERSION \
                    -DdevelopmentVersion=$DEVEL_VERSION \
                    -Dtag=V${RELEASE_VERSION} || exit

# push prepared changes (doing separate just to have control)
git push origin master --tags

# perform release
mvn release:perform -P release || exit

mvn clean deploy
