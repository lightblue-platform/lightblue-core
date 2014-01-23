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
package com.redhat.lightblue.eval;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.query.UpdateExpressionList;
import com.redhat.lightblue.query.SetExpression;
import com.redhat.lightblue.query.UnsetExpression;
import com.redhat.lightblue.query.ForEachExpression;
import com.redhat.lightblue.query.ArrayAddExpression;

/**
 * Base class for update expression evaluators.
 */
public abstract class Updater {

    /**
     * The implementation should update the document
     *
     * @param contextMd The metadata for the current context
     * @param contextPath Absolute path to current context
     *
     * @return true if document is updated, false if not
     */
    public abstract boolean update(JsonDoc doc,FieldTreeNode contextMd,Path contextPath);

    /**
     * Creates an updater object based on the given update expression
     */
    public static Updater getInstance(JsonNodeFactory factory, EntityMetadata md, UpdateExpression expr) {
        return getInstance(factory,md.getFieldTreeRoot(),expr);
    }

    /**
     * Creates an updater object based on the given update expression
     *
     * @param factory Node factory
     * @param context Metadata for the context node
     * @param expr The update expression.
     */
    public static Updater getInstance(JsonNodeFactory factory, FieldTreeNode context, UpdateExpression expr) {
        Updater ret = null;
        if(expr instanceof UpdateExpressionList) {
            ret=new UpdateExpressionListEvaluator(factory,context,(UpdateExpressionList)expr);
        } else if(expr instanceof SetExpression) {
            ret=new SetExpressionEvaluator(factory,context,(SetExpression)expr);
        } else if(expr instanceof UnsetExpression) {
            ret=new UnsetExpressionEvaluator(factory,context,(UnsetExpression)expr);
        } else if(expr instanceof ForEachExpression) {
            ret=new ForEachExpressionEvaluator(factory,context,(ForEachExpression)expr);
        } else if(expr instanceof ArrayAddExpression) {

        }         
        return ret;
    }
}
