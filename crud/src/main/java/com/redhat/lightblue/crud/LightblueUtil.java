/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.crud;

import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.Fields;

/**
 * Utility methods to assist with interacting with the Lightblue framework.
 *
 * @author dcrissman
 */
public final class LightblueUtil {

    public static final String FIELD_OBJECT_TYPE = "objectType";
    public static final String FIELD_ARRAY_COUNT_POSTFIX = "#";

    /**
     * Returns <code>true</code> if the passed in field name is the object type, otherwise <code>false</code>.
     * @param fieldName - field name to test.
     * @return <code>true</code> if the passed in field name is the object type, otherwise <code>false</code>.
     */
    public static boolean isFieldObjectType(String fieldName){
        if(fieldName == null){
            return false;
        }

        return FIELD_OBJECT_TYPE.equalsIgnoreCase(fieldName);
    }

    /**
     * Returns <code>true</code> if the passed in field name matches the array count field name pattern,
     * otherwise <code>false</code>.
     * @param fieldName - field name to test.
     * @return <code>true</code> if the passed in field name matches the array count field name pattern,
     * otherwise <code>false</code>.
     */
    protected static boolean doesFieldNameMatchArrayCountPattern(String fieldName){
        if(fieldName == null){
            return false;
        }
        return fieldName.endsWith(FIELD_ARRAY_COUNT_POSTFIX);
    }

    /**
     * Returns <code>true</code> if the passed in field name is the count field for an array field,
     * otherwise <code>false</code>.
     * @param fieldName - field name to test.
     * @param metadataFields - {@link Fields} from entity metadata.
     * @return <code>true</code> if the passed in field name is the count field for an array field,
     * otherwise <code>false</code>.
     */
    public static boolean isFieldAnArrayCount(String fieldName, Fields metadataFields){
        if(!doesFieldNameMatchArrayCountPattern(fieldName)){
            return false;
        }

        /*
         * Ensure that the actual array field exists also, otherwise it might be a field
         * that simply ends with a '#' character.
         */
        Field field = metadataFields.getField(createArrayFieldNameFromCountField(fieldName));
        if((field != null) && (field instanceof ArrayField)){
            return true;
        }

        return false;
    }

    /**
     * Created and returns the name of the array count field for the passed in array field name.
     * @param arrayFieldName - array field name.
     * @return name of the array count field for the passed in array field name.
     */
    public static String createArrayCountFieldName(String arrayFieldName){
        return arrayFieldName + FIELD_ARRAY_COUNT_POSTFIX;
    }

    /**
     * Creates and returns the array field name for the passed in array count field name.
     * @param countField - array count field.
     * @return the array field name for the passed in array count field name.
     */
    public static String createArrayFieldNameFromCountField(String countField){
        if(countField == null){
            return null;
        }
        if(!doesFieldNameMatchArrayCountPattern(countField)){
            return countField;
        }
        return countField.substring(0, countField.length() - 1);
    }

    private LightblueUtil(){}

}
