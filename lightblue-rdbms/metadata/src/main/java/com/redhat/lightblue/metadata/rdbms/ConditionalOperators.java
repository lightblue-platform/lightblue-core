package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.Enum;

import java.util.Arrays;
import java.util.Set;

public class ConditionalOperators {
    private static final com.redhat.lightblue.metadata.Enum singleton = new Enum("conditionals");
    static {
        singleton.setValues(Arrays.asList("greaterThan", "lessThan", "equalTo", " notEqualTo" , "greaterThanOrEqualTo" , "lessThanOrEqualTo", "in", "notIn", "contains"));
    }

    public static Set<String> getValues() {
        return singleton.getValues();
    }

    public static boolean check(String value) {
        return singleton.getValues().contains(value);
    }
}
