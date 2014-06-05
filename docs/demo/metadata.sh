#!/bin/sh

# requires resttest (https://github.com/jewzaam/pyresttest) and argparse module

LOGGING_LEVEL="debug"
export ENTITY_NAME="nmalik-$(date +'%Y%m%d%H%M%S')";
export ENTITY_VERSION_1="1.0.0";
export ENTITY_VERSION_2="2.0.0"

echo "Running tests for new entity: $ENTITY_NAME"

if [ "x$1" != "x" ]; then
    LOGGING_LEVEL=$1
fi

python -c "import resttest; resttest.main('http://b09.cos.redhat.com:8080', 'metadata-pyresttest.yaml', '$LOGGING_LEVEL')" 2>&1 | tee metadata.log

#unset ENTITY_NAME
#unset ENTITY_VERSION_1
#unset ENTITY_VERSION_2
