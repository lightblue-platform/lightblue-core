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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.crud.Constants;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.query.UnsetExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

/**
 * Removes a field from a doc.
 *
 * Removals take place one by one, results of a removal are visible to the subsequent operations
 */
public class UnsetExpressionEvaluator extends Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnsetExpressionEvaluator.class);

    private static final class AbsPath {
        /**
         * The field to unset
         */
        private final Path field;

        /**
         * If field refers to an array element, the absolute array
         * field containing that element
         */
        private final Path absArrayField;

        /**
         * Absolute path to field
         */
        private final Path absField;

        public AbsPath(Path p,FieldTreeNode fieldNode,Path absField) {
            field=p;
            if(fieldNode instanceof ArrayElement) {
                MutablePath mp=new MutablePath();
                fieldNode.getParent().getFullPath(mp);
                absArrayField = mp.immutableCopy();
            } else {
                absArrayField = null;
            }
            this.absField=absField;
        }

        public String toString() {
            StringBuilder bld = new StringBuilder();
            bld.append(field.toString());
            if (absArrayField != null) {
                bld.append("(arr:").append(absArrayField.toString()).append(")");
            }

            return bld.toString();
        }
    }

    private final List<AbsPath> fields;
    private final JsonNodeFactory factory;

    public UnsetExpressionEvaluator(JsonNodeFactory factory, FieldTreeNode context, UnsetExpression expr) {
        fields = new ArrayList<AbsPath>(expr.getFields().size());
        this.factory = factory;
        for (Path p : expr.getFields()) {
            FieldTreeNode node = context.resolve(p);
            if (node == null) {
                throw new EvaluationError(Constants.ERR_INVLD_DERFRNCE + p);
            }
            fields.add(new AbsPath(p,node,node.getFullPath()));
        }
        LOGGER.debug("context {} fields {}", context, fields);
    }
    
    @Override
    public void getUpdateFields(Set<Path> setFields) {
        for(AbsPath x:fields)
            setFields.add(x.absField);
    }
    
    @Override
    public boolean update(JsonDoc doc,FieldTreeNode contextMd,Path contextPath) {
        boolean ret=false;
        MutablePath p=new MutablePath();
        for(AbsPath x:fields) {
            Path fld=new Path(contextPath,x.field);
            LOGGER.debug("Removing {}",fld);
            if(doc.modify(fld,null,false)!=null) {
                ret=true;
                if(x.absArrayField!=null) {
                    // This is an array
                    p.set(x.absArrayField);
                    p.rewriteIndexes(fld);
                    ArrayNode node = (ArrayNode) doc.get(p);
                    p.setLast(p.getLast() + "#");
                    doc.modify(p, factory.numberNode(node.size()), false);
                }
            }
        }
        return ret;
    }
 }
