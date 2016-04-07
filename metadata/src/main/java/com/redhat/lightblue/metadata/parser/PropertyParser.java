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
        T value=p.getMapProperty(container,name);
        MetadataParser.PropertyType type=p.getType(value);
        if(type==MetadataParser.PropertyType.VALUE) {
            return p.asValue(value);
        } else if(type==MetadataParser.PropertyType.LIST) {
            return parseProperty(p,value);
        } else if(type==MetadataParser.PropertyType.MAP) {
            return parseProperty(p,value);
        }
        return null;
    }
    
    protected Object parseProperty(MetadataParser<T> p,
                                   T property) {
        switch(p.getType(property)) {
        case VALUE: return p.asValue(property);
        case LIST:
            int n=p.getListSize(property);
            ArrayList<Object> resultList=new ArrayList<>(n);
            for(int i=0;i<n;i++) {
                resultList.add(parseProperty(p,p.getListElement(property,i)));
            }
            return resultList;
        case MAP:
            Set<String> children=p.getMapPropertyNames(property);
            Map<String,Object> resultMap=new HashMap<>();
            for(String child:children) {
                resultMap.put(child,parseProperty(p,p.getMapProperty(property,child)));
            }
            return resultMap;
        }
        return null;
    }

    protected T convertProperty(MetadataParser<T> metadataParser,
                                Object propertyValue) {
    	if(propertyValue instanceof List) {    		
            T arr=metadataParser.newList();
            List<Object> list=(List<Object>)propertyValue;
            for(Object x:list) {
                metadataParser.addListElement(arr,convertProperty(metadataParser,x));
            }
            return arr;
    	} else if(propertyValue instanceof Map) {
            T node=metadataParser.newMap();
            Map<String,Object> map=(Map<String,Object>)propertyValue;
            for(Map.Entry<String,Object> entry:map.entrySet()) {
                convertProperty(metadataParser,node,entry.getKey(),entry.getValue());
            }
            return node;   		
    	} else if(propertyValue==null) {
            return null;
        } else {
            return metadataParser.asRepresentation(propertyValue);
    	}
    }
    
    public void convertProperty(MetadataParser<T> metadataParser,
                                T container,
                                String propertyName,
                                Object propertyValue) {
        metadataParser.setMapProperty(container,propertyName,convertProperty(metadataParser,propertyValue));
    }
}
