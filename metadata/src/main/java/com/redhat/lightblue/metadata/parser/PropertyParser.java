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
package com.redhat.lightblue.metadata.parser;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Base class for metadata property parsers. The default behavior is
 * to store unrecognized properties as a java value, list, or map.
 */
public class PropertyParser<T>  {
    
    public Object parseProperty(MetadataParser<T> p,
                                T container,
                                String name) {
        MetadataParser.PropertyType type=p.getType(p.getObjectProperty(container,name));
        if(type==MetadataParser.PropertyType.VALUE) {
            return p.getValueProperty(container,name);
        } else if(type==MetadataParser.PropertyType.LIST) {
            return parseProperty(p,p.getObjectList(container,name));
        } else if(type==MetadataParser.PropertyType.MAP) {
            return parseProperty(p,p.getObjectProperty(container,name));
        }
        return null;
    }

    protected Object parseProperty(MetadataParser<T> p,
                                   Object property) {
        switch(p.getType(property)) {
        case VALUE: return p.getValue(property);
        case LIST:
            ArrayList<Object> resultList=new ArrayList<>();
            List<T> list=p.getObjectList((T)property);
            for(T element:list) {
                resultList.add(parseProperty(p,element));
            }
            return resultList;
        case MAP:
            Set<String> children=p.getChildNames((T)property);
            Map<String,Object> resultMap=new HashMap<>();
            for(String child:children) {
                resultMap.put(child,parseProperty(p,p.getObjectProperty((T)property,child)));
            }
            return resultMap;
        }
        return null;
    }

    protected Object convertProperty(MetadataParser<T> metadataParser,
                                     Object propertyValue) {
    	if(propertyValue instanceof List) {    		
            Object arr=metadataParser.newArray();
            List<Object> list=(List<Object>)propertyValue;
            for(Object x:list) {
                metadataParser.addArrayElement(arr,convertProperty(metadataParser,x));
            }
            return arr;
    	} else if(propertyValue instanceof Map) {
            T node=metadataParser.newNode();
            Map<String,Object> map=(Map<String,Object>)propertyValue;
            for(Map.Entry<String,Object> entry:map.entrySet()) {
                convertProperty(metadataParser,node,entry.getKey(),entry.getValue());
            }
            return node;   		
    	} else {
            return propertyValue;
    	}
    }
    
    public void convertProperty(MetadataParser<T> metadataParser,
                                T container,
                                String propertyName,
                                Object propertyValue) {
        metadataParser.put(container,propertyName,convertProperty(metadataParser,propertyValue));
    }
}
