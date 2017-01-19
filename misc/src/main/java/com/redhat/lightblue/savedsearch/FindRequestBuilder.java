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
package com.redhat.lightblue.savedsearch;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.ClientIdentification;
import com.redhat.lightblue.EntityVersion;

import com.redhat.lightblue.crud.FindRequest;

import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.StringType;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Error;

/**
 * Builds a find request from a saved search
 */
public class FindRequestBuilder {

    public static final String ERR_SAVED_SEARCH_INVALID_TYPE="crud:saved-search:invalidType";
    public static final String ERR_SAVED_SEARCH_NO_DEFAULT_VALUE="crud:saved-search:noDefaultValue";
    public static final String ERR_SAVED_SEARCH_MISSING_PARAM="crud:saved-search:missing";

    private static final Logger LOGGER = LoggerFactory.getLogger(FindRequestBuilder.class);

    /**
     * Given the parameter values supplied by the caller, returns a
     * map containing default values for missing parameters. If there
     * are missing required parameters, throws an Error with missing
     * parameters.
     */
    public static Map<String,String> fillDefaults(JsonNode savedSearch,
                                                  Map<String,String> parameterValues,
                                                  TypeResolver types) {
        Map<String,String> ret=new HashMap<>();
        JsonNode parametersNode=savedSearch.get("parameters");
        if(parametersNode instanceof ArrayNode) {
            for(Iterator<JsonNode> itr=parametersNode.elements();itr.hasNext();) {
                JsonNode parameterNode=itr.next();
                if(parameterNode instanceof ObjectNode) {
                    Type type=null;
                    JsonNode x=parameterNode.get("name");
                    String name=x.asText();
                    x=parameterNode.get("type");
                    if(x!=null) {
                        type=types.getType(x.asText());
                        if(type==null) {
                            throw Error.get(ERR_SAVED_SEARCH_INVALID_TYPE,x.asText());
                        }
                    } else {
                        type=StringType.TYPE;
                    }
                    boolean optional=false;
                    x=parameterNode.get("optional");                    
                    if(x!=null) {
                        optional=x.asBoolean();
                    }
                    if(parameterValues.containsKey(name)) {
                        ret.put(name,(String)StringType.TYPE.cast(type.cast(parameterValues.get(name))));
                    } else if(optional) {
                        // Fill it in using default value                    
                        JsonNode defaultValue=parameterNode.get("defaultValue");
                        if(defaultValue==null)
                            throw Error.get(ERR_SAVED_SEARCH_NO_DEFAULT_VALUE,name);
                        ret.put(name,(String)StringType.TYPE.cast(type.fromJson(defaultValue)));
                    } else {
                        throw Error.get(ERR_SAVED_SEARCH_MISSING_PARAM,name);
                    }                    
                }
            }
        }
        return ret;
    }

    /**
     * Builds a find request from the saved search by rewriting the
     * search bound parameters using parameter values. The search
     * query components can be either strings, or JSON objects. If
     * they are strings, the parameters are replaced first, then the
     * resulting json string is converted to a query/projection. If
     * the components are JSON objects, then the values of those json
     * objects are replaced with parameter values.
     */
    public static FindRequest buildRequest(JsonNode savedSearch,
                                           String entity,
                                           String version,
                                           ClientIdentification clid,
                                           Map<String,String> parameterValues)
        throws IOException {
        FindRequest request=new FindRequest();
        request.setEntityVersion(new EntityVersion(entity,version));
        request.setClientId(clid);
        JsonNode node=savedSearch.get("query");
        if(node instanceof TextNode) {
            request.setQuery(QueryExpression.fromJson(JsonUtils.json(applyParameters(node.asText(),parameterValues))));
        } else {
            request.setQuery(QueryExpression.fromJson(applyParameters(node,parameterValues)));
        }
        node=savedSearch.get("projection");
        if(node instanceof ArrayNode||
           node instanceof ObjectNode) {
            request.setProjection(Projection.fromJson(applyParameters(node,parameterValues)));
        } else if(node instanceof TextNode) {
            request.setProjection(Projection.fromJson(JsonUtils.json(applyParameters(node.asText(),parameterValues))));
        }
        node=savedSearch.get("sort");
        if(node instanceof ArrayNode||
           node instanceof ObjectNode) {
            request.setSort(Sort.fromJson(applyParameters(node,parameterValues)));
        } else if(node instanceof TextNode) {
            request.setSort(Sort.fromJson(JsonUtils.json(applyParameters(node.asText(),parameterValues))));
        }
        node=savedSearch.get("range");
        if(node instanceof ArrayNode) {
            if(node.size()==2) {
                request.setFrom(node.get(0).asLong());
                request.setTo(node.get(1).asLong());
            }
        }
        return request;
    }

    public static JsonNode applyParameters(JsonNode source,Map<String,String> parameters) {
        if(source instanceof ObjectNode) {
            return applyParameters( (ObjectNode) source, parameters);
        } else if(source instanceof ArrayNode) {
            return applyParameters( (ArrayNode)source, parameters);
        } else if(source instanceof TextNode) {
            String olds=source.asText();
            String news=applyParameters(olds,parameters);
            if(olds!=news) {
                return JsonNodeFactory.instance.textNode(news);
            } else {
                return source;
            }
        } else {
            return source;
        }
    }

    public static String applyParameters(String source,Map<String,String> parameters) {
        int n=source.length();
        StringBuilder bld=new StringBuilder(n);
        StringBuilder param=new StringBuilder(32);
        boolean modified=false;
        int state=0;
        for(int i=0;i<n;i++) {
            char c=source.charAt(i);
            switch(state) {
            case 0: // parsing string
                if(c=='$') {
                    state=1;
                } else {
                    bld.append(c);
                }
                break;

            case 1: // $ seen, expect {
                if(c=='{') {
                    state=2;
                } else {
                    bld.append('$');
                    bld.append(c);
                    state=0;
                }
                break;

            case 2: // parsing symbol name
                if(c=='}') {
                    state=0;
                    String symbolName=param.toString();
                    String value=parameters.get(symbolName.trim());
                    if(value==null) {
                        if(parameters.containsKey(symbolName.trim())) {
                            bld.append("null");
                            modified=true;
                        } else {
                            bld.append("${").append(symbolName).append('}');
                        } 
                    } else {
                        modified=true;
                        bld.append(value);
                    }
                    param=new StringBuilder(32);
                } else {
                    param.append(c);
                }
                break;
            }
        }
        if(!modified)
            return source;
        
        switch(state) {
        case 1:
            return bld.append('$').toString();
        case 2:
            return bld.append("${").append(param).toString();
        }
        return bld.toString();
    }

    private static JsonNode applyParameters(ArrayNode source,Map<String,String> parameters) {
        ArrayNode newNode=JsonNodeFactory.instance.arrayNode();
        for(Iterator<JsonNode> itr=source.elements();itr.hasNext();) {
            newNode.add(applyParameters(itr.next(),parameters));
        }
        return newNode;
    }

    private static JsonNode applyParameters(ObjectNode source,Map<String,String> parameters) {
        ObjectNode newNode=JsonNodeFactory.instance.objectNode();
        for(Iterator<Map.Entry<String,JsonNode>> itr=source.fields();itr.hasNext();) {
            Map.Entry<String,JsonNode> elem=itr.next();
            newNode.set(elem.getKey(),applyParameters(elem.getValue(),parameters));
        }
        return newNode;
    }
}

