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

import java.utilList;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.types.Type;
import com.redhat.lightblue.metadata.types.Math;

import com.redhat.lightblue.query.SetExpression;
import com.redhat.lightblue.query.FieldAndRValue;

/**
 * Sets a field value
 */
public class SetExpressionEvaluator extends Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetExpressionEvaluator.class);

    private final List<FieldData> setValues;
    private final UpdateOperator op;

    private static final class FieldData {
        private final Path field;
        private final Type fieldType;
        private final Path refPath;
        private final Type refType;
        private final RValueExpression value;

        public FieldData(Path field,
                         Type t,
                         RValueExpression value) {
            this.field=field;
            this.fieldType=t;
            this.rvalue=value;
        }
    }

    public SetExpressionEvaluator(JsonNodeFactory factory,
                                  FieldTreeNode context,
                                  SetExpression expr) {
        op=expr.getOp();
        for(FieldAndRValue fld:expr.getFields()) {
            Path field=fld.getField();
            RValueExpression rvalue=fld.getRValue();
            Path refPath=null;
            FieldTreeNode refMdNode=null;
            Value value=null;
            if(rvalue.getType()==RValueExpression.RValueType._dereference) {
                refPath=rvalue.getPath();
                refMdNode=context.resolve(refPath);
                if(refMdNode==null)
                    throw new EvaluationError("Cannot access "+refPath);
            } else if(rvalue.getType()==RValueExpression.RValueType._value) {
                value=rvalue.getValue();
            }
            FieldTreeNode mdNode=context.resolve(field);
            if(mdNode==null)
                throw new EvaluationError("Cannot access "+field);

            if(mdNode instanceof SimpleField||
               mdNode instanceof SimpleArrayElement) {
                if(rvalue.getType()==RValueExpression.RValueType._dereference) {
                    if(!mdNode.getType().equals(refMdNode.getType()))
                        throw new EvaluationError("Incompatible dereference "+field+" <- "+refPath);
                } else if(rvalue.getType()==RValueExpression.RValueType._emptyObject)
                    throw new EvaluationError("Incompatible assignment "+field +" <- {}");
                
                data=new FieldData(field,mdNode.getType(),refPath,refMdNode,value);
            } else if(mdNode instanceof ObjectField||
                      mdNode instanceof ObjectArrayElement) {
                // Only a dereference or empty object is acceptable here
                if(rvalue.getType()==RValueExpression.RValueType._dereference) {
                    if(!refMdNode insanceof ObjectField)
                        throw new EvaluationError("Incompatible assignment "+field+" <- "+refPath);
                } else if(rvalue.getType()==RValueExpression.RValueType._value)
                    throw new EvaluationError("Incompatible assignment "+field+" <- "+rvalue.getValue());
                data=new FieldData(field,mdNode.getType(),refPath,refMdNode,value);
            } else if(mdNode instanceof ArrayField) {
                // Unacceptable
                throw new EvaluationError("Assignment error for "+field);
            } 
        }
    }

    @Override
    public boolean update(JsonDoc doc) {
        boolen ret=false;
        LOGGER.debug("Starting");
        for(FieldData df:setValues) {
            LOGGER.debug("Set field {}",df.field);
            JsonNode oldValueNode=null;
            JsonNode newValueNode=null;
            Object newValue=null;
            Type newValueType=null;
            switch(df.value.type) {
            case _emptyObject: 
                newValueNode=factory.objectNode();                
                break;
            case _dereference:
                JsonNode refNode=doc.get(df.refPath);
                if(refNode!=null) {
                    newValueNode=refNode.deepCopy();
                    newValue=df.refType.fromJson(newValueNode);
                    newValueType=df.refType;
                }
                break;
            case _value:
                newValue=df.value.getValue().getValue();
                newValueNode=df.fieldType.toJson(factory,newValue);
                newValueType=df.fieldType;
                break;
            }
            if(op==UpdateOperator._set) {
                oldValueNode=doc.modify(df.field,newValueNode,true);
            } else if(op==UpdateOperator._add) {
                if(newValueNode!=null) {
                    oldValueNode=doc.get(df.field);
                    if(oldValueNode!=null) {
                        newValueNode=df.fieldType.toJson(factory,
                                                         Math.add(df.fieldType.fromJson(oldValueNode),
                                                                  newValue,
                                                                  Math.promote(df.fieldType,newValueType)));
                        doc.modify(df.field,newValueNode,false);
                    }
                }
            }
            if(!ret)
                ret=(oldValue==null&&newValue!=null) ||
                    (oldValue!=null&&newValue==null) ||
                    (oldValue!=null&&newValue!=null&&!oldValue.equals(newValue));
            
        }
        LOGGER.debug("Completed");
        return ret;
    }
 }
