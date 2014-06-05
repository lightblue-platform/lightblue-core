#!/bin/sh

mkdir -p target/test-classes

for x in `ls -1 -d src/test/resources/*json-schema-test*/`;
do
    BASE=`echo $x | sed 's#.*/\([^/]*\)/#\1#g'`
    ls -1 $x*.json | sed 's#src/test/resources/\(.*\)#\1#g' > target/test-classes/junit-files-$BASE.log
#    ls -1 $x | sed 's#src/test/resources/\(.*\)#\1#g' > target/test-classes/junit-files-$BASE.log
done
