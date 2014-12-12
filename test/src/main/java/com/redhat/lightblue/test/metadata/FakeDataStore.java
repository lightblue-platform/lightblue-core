package com.redhat.lightblue.test.metadata;

import com.redhat.lightblue.metadata.DataStore;

public class FakeDataStore implements DataStore{

    private static final long serialVersionUID = 6467478406388312375L;

    private final String backend;

    public FakeDataStore(String backend){
        this.backend = backend;
    }

    @Override
    public String getBackend() {
        return backend;
    }

}
