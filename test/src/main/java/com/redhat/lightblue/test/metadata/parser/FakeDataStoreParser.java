package com.redhat.lightblue.test.metadata.parser;

import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.test.metadata.FakeDataStore;
import com.redhat.lightblue.util.Error;

public class FakeDataStoreParser<NodeType> implements DataStoreParser<NodeType>{

    private final String backend;

    public FakeDataStoreParser(String backend){
        this.backend = backend;
    }

    @Override
    public DataStore parse(String name, MetadataParser<NodeType> p, NodeType node) {
        if (!backend.equals(name)) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
        }

        return new FakeDataStore(backend);
    }

    @Override
    public void convert(MetadataParser<NodeType> p, NodeType emptyNode, DataStore object) {
        //Do Nothing!
    }

    @Override
    public String getDefaultName() {
        return backend;
    }

}
