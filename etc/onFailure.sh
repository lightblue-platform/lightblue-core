#!/usr/bin/env sh

echo "DEBUG BUILD FAILURE"
echo "Current directory is $(pwd)"
echo "Sunfire reports"
for i in `find . -type d -name surefire-reports` ; do for f in $i/*.txt; do echo $f : `cat $f`; done  done
