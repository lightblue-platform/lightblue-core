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

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.JsonDoc;

/**
 * Wraps a JsonDoc or JsonNode as a Comparable item, based on
 * SortFieldInfo. This way, one can sort documents or JsonNoded based
 * on a Sort specification.
 */
public class SortableItem implements Comparable<SortableItem> {
    private final Object[] keyValues;
    private final JsonNode node;
    private final SortFieldInfo[] sortFields;

    
    public SortableItem(JsonNode node,SortFieldInfo[] sortFields) {
        this.node=node;
        this.sortFields=sortFields;
        keyValues=new Object[sortFields.length];
        for(int i=0;i<sortFields.length;i++) {
            JsonNode valueNode=JsonDoc.get(node,sortFields[i].getName());
            keyValues[i]=sortFields[i].getField().getType().fromJson(valueNode);
        }
    }

    public JsonNode getNode() {
        return node;
    }
    
    @Override
    public boolean equals(Object x) {
        try {
            return compareTo((SortableItem)x)==0;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        StringBuilder buff = new StringBuilder();
        
        for(int i=0;i<keyValues.length;i++) {
            buff.append(sortFields[i].isDescending()?"-":"+");
            if(keyValues[i]!=null) {
                buff.append("_");
            } else {
                buff.append(sortFields[i].getField().getType());
            }
        }
        
        return buff.toString().hashCode();
    }
    
    @Override
    public int compareTo(SortableItem el) {
        for(int i=0;i<keyValues.length;i++) {
            int dir=sortFields[i].isDescending()?-1:1;
            if(keyValues[i]==null) {
                if(el.keyValues[i]==null) {
                    ;
                } else {
                    return -1*dir;
                }
            } else {
                if(el.keyValues[i]==null) {
                    return 1*dir;
                } else {
                    int result=sortFields[i].getField().getType().compare(keyValues[i],el.keyValues[i]);
                    if(result!=0)
                        return result*dir;
                }
            }
        }
        return 0;
    }
}
