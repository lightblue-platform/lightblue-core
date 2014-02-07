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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.Arith;
import com.redhat.lightblue.metadata.types.ObjectType;
import com.redhat.lightblue.metadata.types.ReferenceType;
import com.redhat.lightblue.query.FieldAndRValue;
import com.redhat.lightblue.query.RValueExpression;
import com.redhat.lightblue.query.SetExpression;
import com.redhat.lightblue.query.UpdateOperator;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

/**
 * Sets a field value
 */
public class SetExpressionEvaluator extends Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetExpressionEvaluator.class);

    private final List<FieldData> setValues=new ArrayList<FieldData>();
    private final UpdateOperator op;
    private final JsonNodeFactory factory;

    private static final class FieldData {
        private final Path field;
        private final Type fieldType;
        private final Path refPath;
        private final Type refType;
        private final RValueExpression value;

        public FieldData(Path field,
                         Type t,
                         Path refPath,
                         Type refType,
                         RValueExpression value) {
            this.field=field;
            this.fieldType=t;
            this.refPath=refPath;
            this.refType=refType;
            this.value=value;
        }
    }

    /**
     * Creates a set expression evaluator
     * 
     * @param factory Node factory
     * @param context The context from which the expression will be evaluated
     * @param expr The set expression
     *
     * This ctor resolves the field references in expr and stores them
     * to be applied later.
     */
    public SetExpressionEvaluator(JsonNodeFactory factory,
                                  FieldTreeNode context,
                                  SetExpression expr) {
        this.factory=factory;
        op=expr.getOp();
        for(FieldAndRValue fld:expr.getFields()) {
            Path field=fld.getField();
            LOGGER.debug("Parsing setter for {}",field);
            RValueExpression rvalue=fld.getRValue();
            Path refPath=null;
            FieldTreeNode refMdNode=null;
            FieldData data=null;
            if(rvalue.getType()==RValueExpression.RValueType._dereference) {
                refPath=rvalue.getPath();
                refMdNode=context.resolve(refPath);
                if(refMdNode==null) {
                    throw new EvaluationError("Cannot access "+refPath);
                }
                LOGGER.debug("Refpath {}",refPath);
            } 
            FieldTreeNode mdNode=context.resolve(field);
            if(mdNode==null) {
                throw new EvaluationError("Cannot access "+field);
            }
            
            if (mdNode instanceof SimpleField || mdNode instanceof SimpleArrayElement) {
                data=initializeSimple(rvalue, refMdNode, mdNode, field, refPath);
            } else if (mdNode instanceof ObjectField || mdNode instanceof ObjectArrayElement) {
                // Only a dereference or empty object is acceptable here
                data=initializeObject(rvalue, refMdNode, mdNode, field, refPath);
            } else if(mdNode instanceof ArrayField) {
                // Unacceptable
                throw new EvaluationError("Assignment error for "+field);
            } 
            setValues.add(data);
        }
    }

    private FieldData initializeSimple(RValueExpression rvalue, FieldTreeNode refMdNode, FieldTreeNode mdNode, Path field, Path refPath) {
        if (rvalue.getType() == RValueExpression.RValueType._dereference) {
            if (!mdNode.getType().equals(refMdNode.getType())) {
                throw new EvaluationError("Incompatible dereference " + field + " <- " + refPath);
            }
        } else if (rvalue.getType() == RValueExpression.RValueType._emptyObject) {
            throw new EvaluationError("Incompatible assignment " + field + " <- {}");
        } else if (rvalue.getType() == RValueExpression.RValueType._null) {
            return new FieldData(field, mdNode.getType(), refPath, ReferenceType.TYPE, rvalue);
        }
        
        return new FieldData(field, mdNode.getType(), refPath, refMdNode == null ? null : refMdNode.getType(), rvalue);    
    }
    
    private FieldData initializeObject(RValueExpression rvalue, FieldTreeNode refMdNode, FieldTreeNode mdNode, Path field, Path refPath) {
        if (rvalue.getType() == RValueExpression.RValueType._dereference) {
            if (!(refMdNode instanceof ObjectField)) {
                throw new EvaluationError("Incompatible assignment " + field + " <- " + refPath);
            }
        } else if (rvalue.getType() == RValueExpression.RValueType._value) {
            throw new EvaluationError("Incompatible assignment " + field + " <- " + rvalue.getValue());
        } else if (rvalue.getType() == RValueExpression.RValueType._null) {
            return new FieldData(field, mdNode.getType(), refPath, ObjectType.TYPE, rvalue);
        }
        return new FieldData(field, mdNode.getType(), refPath, refMdNode == null ? null : refMdNode.getType(), rvalue);
    }
    
    @Override
    public boolean update(JsonDoc doc, FieldTreeNode contextMd, Path contextPath) {
        boolean ret = false;
        LOGGER.debug("Starting");
        for (FieldData df : setValues) {
            LOGGER.debug("Set field {} in ctx: {}", df.field, contextPath);
            JsonNode oldValueNode = null;
            JsonNode newValueNode = null;
            Object newValue = null;
            Type newValueType = null;
            switch (df.value.getType()) {
                case _null:
                    newValueNode = factory.nullNode();
                    break;
                case _emptyObject:
                    newValueNode = factory.objectNode();
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
                    newValueNode = df.fieldType.toJson(factory, newValue);
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
        Path fieldPath=new Path(contextPath,df.field);
        if(op==UpdateOperator._set) {
            oldValueNode=doc.modify(fieldPath,newValueNode,true);
        } else if(op==UpdateOperator._add) {
            oldValueNode=doc.get(fieldPath);
            if(newValueNode!=null && oldValueNode != null) {
                newValueNode=df.fieldType.toJson(factory, Arith.add(df.fieldType.fromJson(oldValueNode), newValue, Arith.promote(df.fieldType,newValueType)));
                doc.modify(fieldPath,newValueNode,false);
             }
        }
        return oldValueNode;
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
