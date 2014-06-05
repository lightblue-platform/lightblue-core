#!/bin/sh

# requires resttest (https://github.com/jewzaam/pyresttest) and argparse module

LOGGING_LEVEL="debug"

# BIG HACK, expect metadata.sh to be run first and for it NOT to unset env variables..
export ENTITY_VERSION="${ENTITY_VERSION_2}"

echo "Running tests for entity: $ENTITY_NAME"

if [ "x$1" != "x" ]; then
    LOGGING_LEVEL=$1
fi

python -c "import resttest; resttest.main('http://b09.cos.redhat.com:8080', 'data-pyresttest.yaml', '$LOGGING_LEVEL')" 2>&1 | tee data.log

#unset ENTITY_NAME
#unset ENTITY_VERSION
