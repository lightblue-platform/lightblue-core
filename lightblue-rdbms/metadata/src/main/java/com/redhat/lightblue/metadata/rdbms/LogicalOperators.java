package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.Enum;

import java.util.Arrays;
import java.util.Set;

public class LogicalOperators {
    private static final com.redhat.lightblue.metadata.Enum singleton = new Enum("logicalOperators");
    static {
        singleton.setValues(Arrays.asList("and", "or"));
    }

    public static Set<String> getValues() {
        return singleton.getValues();
    }
}
