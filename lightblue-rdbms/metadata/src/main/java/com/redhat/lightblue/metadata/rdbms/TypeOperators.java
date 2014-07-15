package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.Enum;

import java.util.Arrays;
import java.util.Set;

public class TypeOperators {
    private static final Enum singleton = new Enum("operations");
    static {
        singleton.setValues(Arrays.asList("select", "insert", "update", "delete", "call"));
    }

    public static Set<String> getValues() {
        return singleton.getValues();
    }

    public static boolean check(String value) {
        if(singleton.getValues().contains(value)){
            return true;
        }
        return false;
    }
}
