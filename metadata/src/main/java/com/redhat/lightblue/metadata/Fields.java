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
package com.redhat.lightblue.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redhat.lightblue.metadata.EntityMetadata.RootTreeNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

public class Fields implements Serializable {

    private static final long serialVersionUID = 1l;

    private final Map<String, Field> fieldMap = new HashMap<String, Field>();
    private final List<Field> fields = new ArrayList<Field>();

    public int getNumChildren() {
        return fields.size();
    }

    public Field getField(int index) {
        try {
            return fields.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public Field getField(String name) {
        return fieldMap.get(name);
    }

    public Iterator<Field> getFields() {
        return fields.iterator();
    }

    public boolean has(String name) {
        return fieldMap.containsKey(name);
    }

    public void addNew(Field f) {
        String name = f.getName();
        if (has(name)) {
            throw Error.get(Constants.ERR_DUPLICATE_FIELD, name);
        }
        fieldMap.put(name, f);
        fields.add(f);
    }

    public void put(Field f) {
        String name = f.getName();
        if (has(name)) {
            int n = fields.size();
            for (int i = 0; i < n; i++) {
                Field x = fields.get(i);
                if (x.getName().equals(name)) {
                    fields.set(i, f);
                    break;
                }
            }
        } else {
            fields.add(f);
        }
        fieldMap.put(name, f);
    }

    public FieldTreeNode resolve(Path p) {   	
    	return resolve(p, 0);	
    }
    
    private int findPrecedingParentIndex(Path path, int currentPosition) {
        int indexOfParent = 0;
        
       	for(int i=currentPosition;i >= 0; i--) {
       	    String segment = path.head(i);
       	    if(!Path.PARENT.equals(segment)) {
       	        indexOfParent = i;
       	        break;
       	    }
       	}
    	
    	return indexOfParent;
    }
    
    private int findNextNonThisSegment(Path path, int currentPosition) {
        int indexOfSegment = -1;
        
        for(int i=currentPosition;i <= path.numSegments(); i++) {
            String segment = path.head(i);
            if(!Path.THIS.equals(segment)) {
                indexOfSegment = i;
                break;
            }
        }
        
        return indexOfSegment;
    }

    private int findNextNonParentSegment(Path path, int currentPosition) {
        int indexOfSegment = -1;
        
        for(int i=currentPosition;i <= path.numSegments(); i++) {
            String segment = path.head(i);
            if(!Path.PARENT.equals(segment)) {
                indexOfSegment = i;
                break;
            }
        }
        
        return indexOfSegment;
    }
    
    private FieldTreeNode findInNode(FieldTreeNode node, String fieldName) {
        FieldTreeNode found = null;
       
        if((Field)node instanceof SimpleField) {
            SimpleField fieldToSearch = (SimpleField) node;
            if(fieldName.equals(fieldToSearch.getName())) {
                found = fieldToSearch;
            }
        } else if ((Field)node instanceof ObjectField) {
            
        }
                
        return found;
    }
    
    protected FieldTreeNode resolve(Path p, int level) {
        if (level >= p.numSegments()) {
            throw Error.get(Constants.ERR_INVALID_REDIRECTION, p.toString());
        }
        
        String name = p.head(level);
        Error.push(name);
        
        try {     	
            if (p.isIndex(level)) {
                throw Error.get(Constants.ERR_INVALID_ARRAY_REFERENCE);
            }
            if (name.equals(Path.ANY)) {
                throw Error.get(Constants.ERR_INVALID_ARRAY_REFERENCE);
            }
                        
            if(name.equals(Path.THIS)) {
                if(level == 0) {
                    throw Error.get(Constants.ERR_INVALID_THIS);
                } else {
                    String nextFieldName = p.head(findNextNonThisSegment(p, level));
                    Field nextField = getField(nextFieldName);
                    if(nextField == null) {
                        throw Error.get(Constants.ERR_INVALID_FIELD_REFERENCE);
                    } else if (nextField instanceof SimpleField) {
                        return nextField;
                    } else if (nextField instanceof ObjectField) {
                        
                    }
                }
            }
            
            if(name.equals(Path.PARENT)) {
                if(level == 0) {
                    throw Error.get(Constants.ERR_INVALID_THIS);
                } else {
                    for(Field previousField : fields) {    
                        FieldTreeNode parent = previousField.getParent();
                        String nextFieldName = p.head(findNextNonParentSegment(p, level));
                        if(parent instanceof ObjectField) {
                            Iterator<? extends FieldTreeNode> itr = parent.getParent().getChildren();
                            while(itr.hasNext()) {
                                FieldTreeNode childFieldTreeNode = itr.next();
                                if(childFieldTreeNode instanceof EntityMetadata.RootTreeNode) {
                                    return childFieldTreeNode;
                                } else if (childFieldTreeNode instanceof Field){
                                    Field childField = (Field) childFieldTreeNode;
                                    if(nextFieldName.equals(childField.getName())) {
                                        return childField;
                                    }    
                                }
                            }
                        }
                    }
                }
            }
            
            //absolute path
        	Field field = getField(name);
            if (field == null) {
                throw Error.get(Constants.ERR_INVALID_FIELD_REFERENCE);
            }
            return field.resolve(p, level + 1);
            
        } finally {
            Error.pop();
        }
    }

}
