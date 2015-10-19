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

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.types.ArrayType;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;

/**
 * Evaluates lfield op rfield type comparisons. Both fields can be
 * simple fields or arrays. Here's the semantics of the operation:
 *
 * <ul>
 * <li>lfield op rfield : true if lfield op rfield</li>
 * <li>lfield op rarray: true if lfield op rarray[i] for all elements i</li>
 * <li>larray op rarray: true if larray[i] op rarray[i] for all elements i. for op other than $ne, array sizes must be equal</li>
 * </ul>
 *
 *
 */
public class FieldComparisonEvaluator extends QueryEvaluator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldComparisonEvaluator.class);
    
    private final FieldTreeNode fieldMd;
    private final FieldTreeNode rfieldMd;
    private final Path relativePath;
    private final Path rfieldRelativePath;
    private final BinaryComparisonOperator operator;

    /**
     * Constructs evaluator for {field op field} style comparison
     *
     * @param expr The expression
     * @param md Entity metadata
     * @param context The path relative to which the expression will be
     * evaluated
     */
    public FieldComparisonEvaluator(FieldComparisonExpression expr, FieldTreeNode context) {
        this.relativePath = expr.getField();
        this.rfieldRelativePath = expr.getRfield();
        fieldMd = context.resolve(relativePath);
        if (fieldMd == null) {
            throw new EvaluationError(expr, CrudConstants.ERR_FIELD_NOT_THERE + " "+relativePath);
        }
        rfieldMd = context.resolve(rfieldRelativePath);
        if (rfieldMd == null) {
            throw new EvaluationError(expr, CrudConstants.ERR_FIELD_NOT_THERE + " "+rfieldRelativePath);
        }
        // Both fields must be simple fields or simple arrays
        if(!(fieldMd instanceof SimpleField ||
             (fieldMd instanceof ArrayField && ((ArrayField)fieldMd).getElement() instanceof SimpleArrayElement) ) ) {
            throw new EvaluationError(expr, CrudConstants.ERR_EXPECTED_SIMPLE_FIELD_OR_SIMPLE_ARRAY + " "+relativePath);
        }
        if(!(rfieldMd instanceof SimpleField ||
             (rfieldMd instanceof ArrayField && ((ArrayField)rfieldMd).getElement() instanceof SimpleArrayElement) ) ) {
            throw new EvaluationError(expr, CrudConstants.ERR_EXPECTED_SIMPLE_FIELD_OR_SIMPLE_ARRAY + " "+rfieldRelativePath);
        }
        operator = expr.getOp();
        LOGGER.debug("ctor {} {} {}", relativePath, operator, rfieldRelativePath);
    }
    
    
    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        LOGGER.debug("evaluate {} {} {}", relativePath, operator, rfieldRelativePath);
        KeyValueCursor<Path, JsonNode> lcursor = ctx.getNodes(relativePath);
        ctx.setResult(false);
        if (lcursor != null) {
            while (lcursor.hasNext()&&!ctx.getResult()) {
                lcursor.next();
                JsonNode lvalueNode = lcursor.getCurrentValue();
                Object ldocValue=null;
                List<Object> ldocList=null;
                if (lvalueNode != null) {
                    if(fieldMd.getType() instanceof ArrayType) {
                        ldocList=makeList(((ArrayField)fieldMd).getElement().getType(), lvalueNode);
                    } else {
                        ldocValue = fieldMd.getType().fromJson(lvalueNode);
                    }
                }
                KeyValueCursor<Path, JsonNode> rcursor = ctx.getNodes(rfieldRelativePath);
                if (rcursor != null) {
                    while (rcursor.hasNext()&&!ctx.getResult()) {
                        rcursor.next();
                        JsonNode rvalueNode = rcursor.getCurrentValue();
                        Object rdocValue=null;
                        List<Object> rdocList=null;
                        if (rvalueNode != null) {
                            if(rfieldMd.getType() instanceof ArrayType) {
                                rdocList=makeList(((ArrayField)rfieldMd).getElement().getType(),rvalueNode);
                            } else {
                                rdocValue = rfieldMd.getType().fromJson(rvalueNode);
                            }
                        }
                        LOGGER.debug(" lvalue={} rvalue={}", lvalueNode, rvalueNode);
                        if(ldocValue!=null&&rdocValue!=null) {
                            // Both fields are values
                            int result=fieldMd.getType().compare(ldocValue, rdocValue);
                            LOGGER.debug(" result={}", result);
                            if(operator.apply(result)) {
                                ctx.setResult(true);
                            }
                        } else if(ldocList!=null&&rdocList!=null) {
                            // Both fields are arrays. Compare each element
                            Type type=((ArrayField)fieldMd).getElement().getType();
                            int ln=ldocList.size();
                            int rn=rdocList.size();
                            int cmp=0;
                            if(ln==rn) {
                                for(int i=0;i<ln;i++) {
                                    cmp=apply(cmp,type.compare(ldocList.get(i),rdocList.get(i)));
                                }
                            } else {
                                cmp=0x07; // $ne
                            }
                            LOGGER.debug("Comparing arrays {} {} {}={}",ldocList,operator,rdocList,cmp);
                            if(cmpOp(CMP_LOOKUP[cmp],operator)) {
                                ctx.setResult(true);
                            }
                        } else if(ldocList!=null&&rdocValue!=null) {
                            // Left field is an array, right field is a value
                            BinaryComparisonOperator resultOp=lvCompare(rdocValue,ldocList,
                                                                        ((ArrayField)fieldMd).getElement().getType()).invert();
                            LOGGER.debug("Comparing array with field {} {} {}={}",ldocList,operator,rdocValue,resultOp);
                            if(cmpOp(resultOp,operator)) {
                                ctx.setResult(true);
                            }
                        } else if(ldocValue!=null&&rdocList!=null) {
                            // left field is a value, right field is an array
                            BinaryComparisonOperator resultOp=lvCompare(ldocValue,rdocList,fieldMd.getType());
                            LOGGER.debug("Comparing field with array {} {} {}={}",ldocValue,operator,rdocList,resultOp);
                            if(cmpOp(resultOp,operator)) {
                                ctx.setResult(true);
                            }
                        }
                    }
                }
            }
        }
        return ctx.getResult();
    }

    private static List<Object> makeList(Type t,JsonNode node) {
        if(node instanceof ArrayNode) {
            List<Object> list=new ArrayList<>(node.size());
            for(Iterator<JsonNode> itr=((ArrayNode)node).elements();itr.hasNext();) {
                list.add(t.fromJson(itr.next()));
            }
            return list;
        }
        return null;
    }

    /**
     * The <code>result</code> is the result obtained from lvCompare. The <code>op</code> is
     * the operator for the evaluator. Returns if the result satisties the operator.
     */
    private static boolean cmpOp(BinaryComparisonOperator result,BinaryComparisonOperator op) {
        if(result!=op) {
            switch(op) {
            case _neq:
                if(result==BinaryComparisonOperator._eq) {
                    return false;
                }
                break;
            case _gte:
                if(result!=BinaryComparisonOperator._gte&&
                   result!=BinaryComparisonOperator._gt&&
                   result!=BinaryComparisonOperator._eq) {
                    return false;
                }
                break;
            case _lte:
                if(result!=BinaryComparisonOperator._lte&&
                   result!=BinaryComparisonOperator._lt&&
                   result!=BinaryComparisonOperator._eq) {
                    return false;
                }
                break;  
            default:
                return false;
            }
        }
        return true;
    }

    /**
     * Comparison lookup table. A 3-bit value is the index into this table Bit0 means
     * there are greater values. Bit1 means there are equal values. Bit2 means 
     * there are less values.
     */
    private static final BinaryComparisonOperator CMP_LOOKUP[]= {
        BinaryComparisonOperator._neq,  // 000
        BinaryComparisonOperator._gt,   // 001
        BinaryComparisonOperator._eq,   // 010
        BinaryComparisonOperator._gte,  // 011
        BinaryComparisonOperator._lt,   // 100
        BinaryComparisonOperator._neq,  // 101
        BinaryComparisonOperator._lte,  // 110
        BinaryComparisonOperator._neq   // 111
    };

    private static int apply(int cmp,int result) {
        if(result==0) {
            return cmp|0x02;
        } else if(result<0) {
            return cmp|0x04;
        } else {
            return cmp|0x01;
        }
    }

    /**
     * Compare a value to a list. Returns:
     * <ul>
     * <li>_eq: if all values in list are equal to value</li>
     * <li>_lt: if value is less than all values in the list</li>
     * <li>_lte: if value is less than or equal to all the values in the list</li>
     * <li>_gt: if value is greater than all values in the list</li>
     * <li>_gte: if value is greater or equal to all the values in the list</li>
     * <li>_neq: otherwise</li>
     * <ul>
     */
    private static BinaryComparisonOperator lvCompare(Object value,List<Object> list,Type t) {
        int cmp=0;        
        if(value!=null) {
            if(list!=null&&!list.isEmpty()) {
                for(Object x:list) {
                    cmp=apply(cmp,t.compare(value,x));
                }
            }
        } 
        return CMP_LOOKUP[cmp];
    }

}
