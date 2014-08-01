/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.redhat.lightblue.metadata.rdbms.enums;

import com.redhat.lightblue.metadata.rdbms.model.Conditional;
import com.redhat.lightblue.metadata.rdbms.model.Expression;
import com.redhat.lightblue.metadata.rdbms.model.For;
import com.redhat.lightblue.metadata.rdbms.model.ForEach;
import com.redhat.lightblue.metadata.rdbms.model.Statement;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author lcestari
 */
public class ExpressionOperators {
private static final com.redhat.lightblue.metadata.Enum singleton = new com.redhat.lightblue.metadata.Enum("ExpressionOperators");

    //public static final String CONDITIONAL = Conditional.class.getSimpleName(); //-> error constant string expression required 
    public static final String CONDITIONAL = "Conditional";
    public static final String FOR = "For";
    public static final String FOREACH = "ForEach";
    public static final String STATEMENT = "Statement";

    static {
        singleton.setValues(Arrays.asList(CONDITIONAL, FOR, FOREACH, STATEMENT));
    }

    public static Set<String> getValues() {
        return singleton.getValues();
    }

    public static boolean check(String value) {
        return singleton.getValues().contains(value);
    }
}
