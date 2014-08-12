#!/bin/sh

for f in *.id ; do
   sed "s/@id/${1}/" $f > ${f%%.id}.json
done


