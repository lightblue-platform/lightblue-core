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
package com.redhat.lightblue.assoc;

public final class AssocConstants {

    public static final String ERR_CANNOT_CREATE_CHOOSER="assoc:CannotCreateQueryPlanChooser";
    public static final String ERR_UNRELATED_ENTITY_Q="assoc:unsupported:QueryForUnrelatedEntities";
    public static final String ERR_MORE_THAN_TWO_Q="assoc:unsupported:QueryForMoreThanTwoEntities";
    public static final String ERR_REWRITE="assoc:QueryRewriteError";
    public static final String ERR_ARRAY_EXPECTED="assoc:ArrayFieldExpected";
    public static final String ERR_CANNOT_FIND_FIELD="assoc:NoField";
    public static final String ERR_INVALID_QUERYPLAN="assoc:InvalidQueryPlan";

    private AssocConstants() {}
}
