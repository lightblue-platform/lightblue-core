#!/bin/sh

# requires resttest (https://github.com/jewzaam/pyresttest) and argparse module

python -c "import resttest; resttest.main('http://b09.cos.redhat.com:8080', 'metadata-pyresttest.yaml')"
