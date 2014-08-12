#!/bin/sh

while true ; do
  smonkey create-users.json > create-100-users.json
  ./PUT  ${host}/data/user/2.0.0 @create-100-users.json
done

