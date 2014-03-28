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
package com.redhat.lightblue.crud;

import com.redhat.lightblue.JsonNodeBuilder;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.UpdateExpression;

/**
 *
 * @author nmalik
 */
public class CRUDJsonNodeBuilder extends JsonNodeBuilder {
    public JsonNodeBuilder add(String key, QueryExpression value) {
        if (include(value)) {
            getRoot().put(key, value.toString().toLowerCase());
        }
        return this;

    }

    public JsonNodeBuilder add(String key, Projection value) {
        if (include(value)) {
            getRoot().put(key, value.toString());
        }
        return this;
    }

    public JsonNodeBuilder add(String key, UpdateExpression value) {
        if (include(value)) {
            getRoot().put(key, value.toString());
        }
        return this;
    }
}
