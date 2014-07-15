package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.*;

import java.util.Arrays;
import java.util.Set;

public class LoopOperators {
    private static final com.redhat.lightblue.metadata.Enum singleton = new com.redhat.lightblue.metadata.Enum("loopOperator");
    static {
        singleton.setValues(Arrays.asList("$fail", "$continue", "$break"));
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
