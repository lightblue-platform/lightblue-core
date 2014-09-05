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

/**
 * A composite schema is an extension of EntitySchema that replaces
 * all reference fields with references entity schema information. It
 * contains a shallow copy of the EntitySchema instance it is created
 * from, except that the field tree uses a deep copy, with shared leaf
 * nodes. That is, all leaf objects of the original field tree are
 * shared with the original schema instance.
 */
public class CompositeSchema extends EntitySchema {

    /**
     * Constructs a composite schema instance as a shallow copy of the
     * root schema, except that the fields are empty
     */
    private CompositeSchema(EntitySchema root) {
        super(root);
        this.fields=new Fields(fieldRoot);
    }

    public static CompositeSchema newSchemaWithEmptyFields(EntitySchema schema) {
        return new CompositeSchema(schema);
    }

}
