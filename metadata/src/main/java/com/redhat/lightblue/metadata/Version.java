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

package com.redhat.lightblue.metadata;

import java.io.Serializable;
import java.util.Arrays;

public class Version implements Serializable {

    private static final long serialVersionUID = 1l;

    private String value;
    private String[] extendsVersions;
    private String changelog;

    public Version() {}

    public Version(String value,String[] extendsVersions,String changeLog) {
        this.value=value;
        if (extendsVersions != null) {
            this.extendsVersions=Arrays.copyOf(extendsVersions, extendsVersions.length);
        } else {
            this.extendsVersions=null;
        }
        this.changelog=changeLog;
    }

    /**
     * Gets the value of value
     * 
     * @return the value of value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of value
     * 
     * @param argValue
     *            Value to assign to this.value
     */
    public void setValue(String argValue) {
        this.value = argValue;
    }

    /**
     * Gets the value of extendsVersions
     * 
     * @return the value of extendsVersions
     */
    public String[] getExtendsVersions() {
        return extendsVersions==null?null:
            (String[])this.extendsVersions.clone();
    }

    /**
     * Sets the value of extendsVersions
     * 
     * @param argExtendsVersions
     *            Value to assign to this.extendsVersions
     */
    public void setExtendsVersions(String[] argExtendsVersions) {
        this.extendsVersions = argExtendsVersions==null?null:
            (String[])argExtendsVersions.clone();
    }

    /**
     * Gets the value of changelog
     * 
     * @return the value of changelog
     */
    public String getChangelog() {
        return this.changelog;
    }

    /**
     * Sets the value of changelog
     * 
     * @param argChangelog
     *            Value to assign to this.changelog
     */
    public void setChangelog(String argChangelog) {
        this.changelog = argChangelog;
    }

}
