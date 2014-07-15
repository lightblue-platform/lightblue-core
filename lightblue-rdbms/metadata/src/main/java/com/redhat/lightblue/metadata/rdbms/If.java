package com.redhat.lightblue.metadata.rdbms;


import java.util.List;

public class If <Z extends If>{

    private List<Z> conditions;

    public void setConditions(List<Z> conditions) {
        this.conditions = conditions;
    }

    public List<Z> getConditions() {
        return conditions;
    }
}
