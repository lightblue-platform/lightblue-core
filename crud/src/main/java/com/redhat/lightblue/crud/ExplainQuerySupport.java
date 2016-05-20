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

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;

import com.redhat.lightblue.util.JsonDoc;

/**
 * If the controller supports explain query, implement this interface
 */
public interface ExplainQuerySupport {

    /**
     * Explains a query
     *
     * @param ctx Operation context
     * @param entity The entity to search for
     * @param query The query. Cannot be null
     * @param projection What fields to return. Cannot be null
     * @param sort Sort keys. Can be null
     * @param from starting index in the result set. Can be null. Meaninguful
     * only if a sort is given. Starts from 0.
     * @param to end index in the result set. Starts from 0, and inclusive. Can
     * be null.
     * @param destDoc This document is populated with the explained query
     */
    void explain(CRUDOperationContext ctx,
                 QueryExpression query,
                 Projection projection,
                 Sort sort,
                 Long from,
                 Long to,
                 JsonDoc destDoc);
                 
}
