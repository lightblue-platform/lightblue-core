#!/bin/sh

if [[ "X${host}" == "X" ]] ; then
  echo "Set host"
  exit
fi

cmdline="curl -H Content-Type:application/json"

haspwd=0
for upwd in "$@" ; do
  if [[ $upwd == "-u" ]] ; then
     haspwd=1
  fi
done

if [[ $haspwd == 0 ]] ; then
  if [[ "X${userpwd}" != "X" ]] ; then
    cmdline="${cmdline} -u ${userpwd}"
  fi
fi

eval $cmdline $*|tee /tmp/out.json
