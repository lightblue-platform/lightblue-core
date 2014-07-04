package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.Enum;

import java.util.Arrays;
import java.util.Set;

public class Types {
    private static final Enum singleton = new Enum("operations");
    static {
        singleton.setValues(Arrays.asList("select", "insert", "update", "delete"));
    }

    public static Set<String> getValues() {
        return singleton.getValues();
    }

}
