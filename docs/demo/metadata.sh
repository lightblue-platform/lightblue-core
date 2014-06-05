#!/bin/sh

# requires resttest (https://github.com/jewzaam/pyresttest) and argparse module

LOGGING_LEVEL="debug"

if [ "x$1" != "x" ]; then
    LOGGING_LEVEL=$1
fi

python -c "import resttest; resttest.main('http://b09.cos.redhat.com:8080', 'metadata-pyresttest-basic.yaml', '$LOGGING_LEVEL')"
