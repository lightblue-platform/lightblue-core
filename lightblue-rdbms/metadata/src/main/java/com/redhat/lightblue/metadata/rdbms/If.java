package com.redhat.lightblue.metadata.rdbms;


import com.redhat.lightblue.metadata.parser.MetadataParser;

import java.util.List;

public abstract class If <Z extends If> implements ComplexConverter{

    private List<Z> conditions;

    public void setConditions(List<Z> conditions) {
        this.conditions = conditions;
    }

    public List<Z> getConditions() {
        return conditions;
    }

}
