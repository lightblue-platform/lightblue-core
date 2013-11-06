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

package com.redhat.lightblue.metadata.constraints;

import java.io.Serializable;

public final class Reference implements Serializable {

    private static final long serialVersionUID=1l;

    private String entityName;
    private String versionValue;
    private String thisField;
    private String entityField;


    /**
     * Gets the value of entityName
     *
     * @return the value of entityName
     */
    public String getEntityName() {
        return this.entityName;
    }

    /**
     * Sets the value of entityName
     *
     * @param argEntityName Value to assign to this.entityName
     */
    public void setEntityName(String argEntityName) {
        this.entityName = argEntityName;
    }

    /**
     * Gets the value of versionValue
     *
     * @return the value of versionValue
     */
    public String getVersionValue() {
        return this.versionValue;
    }

    /**
     * Sets the value of versionValue
     *
     * @param argVersionValue Value to assign to this.versionValue
     */
    public void setVersionValue(String argVersionValue) {
        this.versionValue = argVersionValue;
    }

    /**
     * Gets the value of thisField
     *
     * @return the value of thisField
     */
    public String getThisField() {
        return this.thisField;
    }

    /**
     * Sets the value of thisField
     *
     * @param argThisField Value to assign to this.thisField
     */
    public void setThisField(String argThisField) {
        this.thisField = argThisField;
    }

    /**
     * Gets the value of entityField
     *
     * @return the value of entityField
     */
    public String getEntityField() {
        return this.entityField;
    }

    /**
     * Sets the value of entityField
     *
     * @param argEntityField Value to assign to this.entityField
     */
    public void setEntityField(String argEntityField) {
        this.entityField = argEntityField;
    }

}
