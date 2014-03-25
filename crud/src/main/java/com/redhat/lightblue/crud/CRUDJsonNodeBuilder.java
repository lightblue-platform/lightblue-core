/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
