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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;

/**
 * Request to find documents
 */
public class FindRequest extends Request {

    private final CRUDFindRequest cfr=new CRUDFindRequest();

    /**
     * The query
     */
    public QueryExpression getQuery() {
        return cfr.getQuery();
    }

    /**
     * The query
     */
    public void setQuery(QueryExpression q) {
        cfr.setQuery(q);
    }

    /**
     * Specifies what fields of the documents to return
     */
    public Projection getProjection() {
        return cfr.getProjection();
    }

    /**
     * Specifies what fields of the documents to return
     */
    public void setProjection(Projection x) {
        cfr.setProjection(x);
    }

    /**
     * Specifies the order in which the documents will be returned
     */
    public Sort getSort() {
        return cfr.getSort();
    }

    /**
     * Specifies the order in which the documents will be returned
     */
    public void setSort(Sort s) {
        cfr.setSort(s);
    }

    /**
     * Specifies the index in the result set to start returning documents.
     * Meaningful only if sort is given. Starts from 0.
     */
    public Long getFrom() {
        return cfr.getFrom();
    }

    /**
     * Specifies the index in the result set to start returning documents.
     * Meaningful only if sort is given. Starts from 0.
     */
    public void setFrom(Long l) {
        cfr.setFrom(l);
    }

    /**
     * Specifies the last index of the document in the result set to be
     * returned. Meaningful only if sort is given. Starts from 0.
     */
    public Long getTo() {
        return cfr.getTo();
    }

    /**
     * Specifies the last index of the document in the result set to be
     * returned. Meaningful only if sort is given. Starts from 0.
     */
    public void setTo(Long l) {
        cfr.setTo(l);
    }

    public CRUDFindRequest getCRUDFindRequest() {
        return cfr;
    }

    public void shallowCopyFrom(FindRequest r) {
        shallowCopyFrom(r, r.getCRUDFindRequest());
    }

    public void shallowCopyFrom(Request r, CRUDFindRequest c) {
        super.shallowCopyFrom(r);
        this.cfr.shallowCopyFrom(c);
    }

    public CRUDOperation getOperation() {
        return CRUDOperation.FIND;
    }
    
    /**
     * Returns JSON representation of this
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        getCRUDFindRequest().toJson(getFactory(), node);
        return node;
    }

    /**
     * Parses a find request from a json object. Unrecognized elements are
     * ignored.
     */
    public static FindRequest fromJson(ObjectNode node) {
        FindRequest req = new FindRequest();
        req.parse(node);
        req.getCRUDFindRequest().fromJson(node);
        return req;
    }
}
