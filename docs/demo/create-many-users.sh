#!/bin/sh

#while true ; do
  smonkey create-users.json > create-100-users.json
  curl -X PUT -H Content-Type:application/json -d @create-100-users.json http://localhost:8080/data/user/2.0|tee out.json
#done

