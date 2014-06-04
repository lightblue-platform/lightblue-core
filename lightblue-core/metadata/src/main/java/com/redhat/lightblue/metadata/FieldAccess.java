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

public class FieldAccess implements Serializable, MetadataAccess {

    private static final long serialVersionUID = 1l;

    private final Access find = new Access();
    private final Access update = new Access();
    private final Access insert = new Access();

    /**
     * Gets the value of find
     *
     * @return the value of find
     */
    public Access getFind() {
        return this.find;
    }

    /**
     * Gets the value of update
     *
     * @return the value of update
     */
    public Access getUpdate() {
        return this.update;
    }

    /**
     * Gets the value of insert
     *
     * @return the value of insert
     */
    public Access getInsert() {
        return this.insert;
    }

}
