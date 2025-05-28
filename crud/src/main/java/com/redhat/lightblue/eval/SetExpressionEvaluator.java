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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.Arith;
import com.redhat.lightblue.query.FieldAndRValue;
import com.redhat.lightblue.query.MaskedSetExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.ProjectionList;
import com.redhat.lightblue.query.RValueExpression;
import com.redhat.lightblue.query.SetExpression;
import com.redhat.lightblue.query.UpdateOperator;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

/**
 * Sets a field value
 */
public class SetExpressionEvaluator extends Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetExpressionEvaluator.class);

    private final List<FieldData> setValues = new ArrayList<>();
    private final UpdateOperator op;
    private final JsonNodeFactory factory;
    private JsonDoc project;
    private boolean masked;

    private static final class FieldData {
        /**
         * Relative path to the field to set
         */
        private final Path field;
        /**
         * Absolute path to the field to set. The absolute path may contain '*'
         */
        private final Path absField;

        /**
         * Type of the field to set
         */
        private final Type fieldType;

        /**
         * If the field is to be set from another field, the referenced relative path to the source field
         */
        private final Path refPath;

        /**
         * If the field is to be set from another field, the type of the source field
         */
        private final Type refType;

        /**
         * If the field is set to a value, the value
         */
        private final RValueExpression value;

        public FieldData(Path field, Type t, Path refPath, Type refType, RValueExpression value, Path absField) {
            this.field = field;
            this.fieldType = t;
            this.refPath = refPath;
            this.refType = refType;
            this.value = value;
            this.absField = absField;
        }
    }

    /**
     * Creates a set expression evaluator
     *
     * @param factory Node factory
     * @param context The context from which the expression will be evaluated
     * @param expr The set expression
     *
     * This ctor resolves the field references in expr and stores them to be
     * applied later.
     */
    public SetExpressionEvaluator(JsonNodeFactory factory, FieldTreeNode context, SetExpression expr) {
        this.factory = factory;
        op = expr.getOp();
        List<JsonDoc> docs = new ArrayList<JsonDoc>();
        Projector projector = null;
        if (expr instanceof MaskedSetExpression) {
            MaskedSetExpression mExpr = (MaskedSetExpression) expr;
            masked = true;
            projector = Projector.getInstance(new ProjectionList(mExpr.getMaskFields()), Path.EMPTY, context);
        }
        for (FieldAndRValue fld : expr.getFields()) {
            Path field = fld.getField();
            RValueExpression rvalue = fld.getRValue();
            Path refPath = null;
            FieldTreeNode refMdNode = null;
            FieldData data = null;
            if (rvalue.getType() == RValueExpression.RValueType._dereference) {
                refPath = rvalue.getPath();
                refMdNode = context.resolve(refPath);
                if (refMdNode == null) {
                    throw new EvaluationError(CrudConstants.ERR_CANT_ACCESS + refPath);
                }
                LOGGER.debug("Refpath {}", refPath);
            }
            if(rvalue.getType() == RValueExpression.RValueType._value && rvalue.getValue().getValue() instanceof ObjectNode && projector != null){
                ObjectNode node = (ObjectNode) rvalue.getValue().getValue();
                docs.add(projector.project(new JsonDoc(node), factory));
            }
            FieldTreeNode mdNode = context.resolve(field);
            if (mdNode == null) {
                throw new EvaluationError(CrudConstants.ERR_CANT_ACCESS + field);
            }

            if (mdNode instanceof SimpleField || mdNode instanceof SimpleArrayElement) {
                data = initializeSimple(rvalue, refMdNode, mdNode, field, refPath);
            } else if (mdNode instanceof ObjectField || mdNode instanceof ObjectArrayElement) {
                data = initializeObject(rvalue, refMdNode, mdNode, field, refPath);
            } else if (mdNode instanceof ArrayField) {
                data = initializeArray(rvalue, refMdNode, mdNode, field, refPath);
            }
            setValues.add(data);
        }
        project = new JsonDoc(JsonDoc.listToDoc(docs, factory));
    }

    private FieldData initializeSimple(RValueExpression rvalue, FieldTreeNode refMdNode, FieldTreeNode mdNode, Path field, Path refPath) {
        if (rvalue.getType() == RValueExpression.RValueType._dereference) {
            if (!mdNode.getType().equals(refMdNode.getType())) {
                throw new EvaluationError(CrudConstants.ERR_INCOMPATIBLE_DEREFERENCE + field + " <- " + refPath);
            }
        } else if (rvalue.getType() != RValueExpression.RValueType._null) {
            Value v = rvalue.getValue();
            if (v.getValue() instanceof JsonNode) {
                throw new EvaluationError(CrudConstants.ERR_INCOMPATIBLE_ASSIGNMENT + field + " <- {}");
            }
        }

        return new FieldData(field, mdNode.getType(), refPath, refMdNode == null ? null : refMdNode.getType(), rvalue, mdNode.getFullPath());
    }

    private FieldData initializeObject(RValueExpression rvalue, FieldTreeNode refMdNode, FieldTreeNode mdNode, Path field, Path refPath) {
        if (rvalue.getType() == RValueExpression.RValueType._dereference) {
            if (!(refMdNode instanceof ObjectField)) {
                throw new EvaluationError(CrudConstants.ERR_INCOMPATIBLE_ASSIGNMENT + field + " <- " + refPath);
            }
        } else if (rvalue.getType() != RValueExpression.RValueType._null) {
            Value v = rvalue.getValue();
            if (!(v.getValue() instanceof ObjectNode)) {
                throw new EvaluationError(CrudConstants.ERR_INCOMPATIBLE_ASSIGNMENT + field + " <- " + rvalue.getValue());
            }
        }
        return new FieldData(field, mdNode.getType(), refPath, refMdNode == null ? null : refMdNode.getType(), rvalue, mdNode.getFullPath());
    }

    private FieldData initializeArray(RValueExpression rvalue, FieldTreeNode refMdNode, FieldTreeNode mdNode, Path field, Path refPath) {
        if (rvalue.getType() == RValueExpression.RValueType._dereference) {
            if (!(refMdNode instanceof ArrayField)) {
                throw new EvaluationError(CrudConstants.ERR_INCOMPATIBLE_ASSIGNMENT + field + " <- " + refPath);
            }
        } else if (rvalue.getType() != RValueExpression.RValueType._null) {
            Value v = rvalue.getValue();
            if (!(v.getValue() instanceof ArrayNode)) {
                throw new EvaluationError(CrudConstants.ERR_INCOMPATIBLE_ASSIGNMENT + field + " <- " + rvalue.getValue());
            }
        }
        return new FieldData(field, mdNode.getType(), refPath, refMdNode == null ? null : refMdNode.getType(), rvalue, mdNode.getFullPath());
    }

    @Override
    public void getUpdateFields(Set<Path> fields) {
        for (FieldData df : setValues) {
            fields.add(df.absField);
        }
    }

    @Override
    public boolean update(JsonDoc doc, FieldTreeNode contextMd, Path contextPath) {
        boolean ret = false;

        LOGGER.debug("Starting");
        for (FieldData df : setValues) {
            LOGGER.debug("Set field {} in ctx: {} to {}/{}", df.field, contextPath, df.value, df.value.getType());
            JsonNode oldValueNode;
            JsonNode newValueNode = null;
            Object newValue = null;
            Type newValueType = null;
            switch (df.value.getType()) {
                case _null:
                    newValueNode = factory.nullNode();
                    break;
                case _dereference:
                    JsonNode refNode = doc.get(new Path(contextPath, df.refPath));
                    if (refNode != null) {
                        newValueNode = refNode.deepCopy();
                        newValue = df.refType.fromJson(newValueNode);
                        newValueType = df.refType;
                    }
                    break;
                case _value:
                    newValue = df.value.getValue().getValue();
                    newValueNode = newValue instanceof JsonNode ? (JsonNode) newValue : df.fieldType.toJson(factory, newValue);
                    newValueType = df.fieldType;
                    break;
            }
            oldValueNode = setOrAdd(doc, contextPath, df, newValueNode, newValue, newValueType);
            if (!ret) {
                ret = oldAndNewAreDifferent(oldValueNode, newValueNode);
            }
        }
        LOGGER.debug("Completed");
        return ret;
    }

    private JsonNode setOrAdd(JsonDoc doc, Path contextPath, FieldData df, JsonNode newValueNode, Object newValue, Type newValueType) {
        JsonNode oldValueNode = null;
        Path fieldPath = new Path(contextPath, df.field);
        if (op == UpdateOperator._set) {
            LOGGER.debug("set fieldPath={}, newValue={}", fieldPath, newValueNode);
            oldValueNode = doc.modify(fieldPath, newValueNode, true);
        } else if (op == UpdateOperator._add) {
            oldValueNode = doc.get(fieldPath);
            if (newValueNode != null && oldValueNode != null) {
                newValueNode = df.fieldType.toJson(factory, Arith.add(df.fieldType.fromJson(oldValueNode), newValue, Arith.promote(df.fieldType, newValueType)));
                if (masked) {
                    copyProjection(project, doc, fieldPath);
                } else {
                    doc.modify(fieldPath, newValueNode, false);
                }
            }
        }
        return oldValueNode;
    }
    
    private void copyProjection(JsonDoc projection, JsonDoc docToModify, Path fieldPath){
        Iterator<Entry<String, JsonNode>> fieldsIt = project.getRoot().fields();
        while (fieldsIt.hasNext()) {
            Entry<String, JsonNode> next = fieldsIt.next();
            docToModify.modify(fieldPath.add(new Path(next.getKey())), next.getValue(), false);
        }
    }

    private boolean oldAndNewAreDifferent(JsonNode oldValueNode, JsonNode newValueNode) {
        if (oldValueNode == null && newValueNode != null) {
            return true;
        } else if (oldValueNode != null && newValueNode == null) {
            return true;
        } else if (oldValueNode != null && newValueNode != null) {
            return !oldValueNode.equals(newValueNode);
        } else {
            return false;
        }
    }

}
