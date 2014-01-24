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
package com.redhat.lightblue.query;

import java.util.Map;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Array forEach expression
 * <pre>
 *  { $foreach : { path : update_query_expression,   
 *                             $update : foreach_update_expression } }  
 * </pre>
 */
public class ForEachExpression extends ArrayUpdateExpression {
    
    private static final long serialVersionUID = 1L;
    private final Path field;
    private final QueryExpression query;
    private final UpdateExpression update;
    
    /**
     * Constructs a foreach expression using the values
     */
    public ForEachExpression(Path field,QueryExpression query,UpdateExpression update) {
        this.field=field;
        this.query=query;
        this.update=update;
    }

    /**
     * The array field to operate on
     */
    public Path getField() {
        return field;
    }

    /**
     * The query to select array elements
     */
    public QueryExpression getQuery() {
        return query;
    }

    /**
     * The operation to be performed on the selected array elements
     */
    public UpdateExpression getUpdate() {
        return update;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node=getFactory().objectNode();
        ObjectNode opNode=getFactory().objectNode();
        opNode.set(field.toString(),query.toJson());
        opNode.set("$update",update.toJson());
        node.set("$foreach",opNode);
        return node;
    }
    
    /**
     * Parses a foreach expression from the given json object
     */
    public static ForEachExpression fromJson(ObjectNode node) {
        if(node.size()==1) {
            JsonNode argNode=node.get("$foreach");
            if(argNode instanceof ObjectNode) {
                ObjectNode objArg=(ObjectNode)argNode;
                if(objArg.size()==2) {
                    JsonNode updateNode=null;
                    JsonNode queryNode=null;
                    Path field=null;
                    for(Iterator<Map.Entry<String,JsonNode>> itr=objArg.fields();itr.hasNext();) {
                        Map.Entry<String,JsonNode> entry=itr.next();
                        if("$update".equals(entry.getKey())) {
                            updateNode=entry.getValue();
                        } else {
                            field=new Path(entry.getKey());
                            queryNode=entry.getValue();
                        }
                    }
                    if(queryNode!=null&&updateNode!=null&&field!=null) {
                        return new ForEachExpression(field,UpdateQueryExpression.fromJson(queryNode),
                                ForEachUpdateExpression.fromJson(updateNode));
                    }
                }
            }
        }
        throw Error.get(ERR_INVALID_ARRAY_UPDATE_EXPRESSION,node.toString());
    }
}
