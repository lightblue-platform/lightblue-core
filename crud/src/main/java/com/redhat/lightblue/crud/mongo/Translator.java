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
package com.redhat.lightblue.crud.mongo;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.NullNode;

import com.mongodb.BasicDBObject;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonNodeCursor;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.mediator.MetadataResolver;

/**
 * Translations between BSON and JSON
 */
public class Translator {

    public static final String ERR_NO_OBJECT_TYPE="NO_OBJECT_TYPE";
    public static final String ERR_INVALID_OBJECTTYPE="INVALID_OBJECTTYPE";
    public static final String ERR_INVALID_FIELD="INVALID_FIELD";

    private static final Logger logger=LoggerFactory.getLogger(Translator.class);

    private static final Path OBJECT_TYPE=new Path("object_type");

    private final MetadataResolver mdResolver;

    public Translator(MetadataResolver mdResolver) {
        this.mdResolver=mdResolver;
    }
    
    public BasicDBObject[] toBson(List<JsonDoc> docs) {
        BasicDBObject[] ret=new BasicDBObject[docs.size()];
        int i=0;
        for(JsonDoc doc:docs)
            ret[i++]=toBson(doc);
        return ret;
    }

    public BasicDBObject toBson(JsonDoc doc) {
        logger.debug("toBson() enter");
        JsonNode node=doc.get(OBJECT_TYPE);
        if(node==null)
            throw Error.get(ERR_NO_OBJECT_TYPE);
        EntityMetadata md=mdResolver.getEntityMetadata(node.asText());
        if(md==null)
            throw Error.get(ERR_INVALID_OBJECTTYPE,node.asText());
        return toBson(doc,md);
    }

    private BasicDBObject toBson(JsonDoc doc,EntityMetadata md) {
        logger.debug("Entity: {}",md.getName());
        BasicDBObject ret=null;
        JsonNodeCursor cursor=doc.cursor();
        if(cursor.firstChild()) {
            ret=objectToBson(cursor,md);
        }
        logger.debug("toBson() return");
        return ret;
    }

    private Object toValue(Type t,JsonNode node) {
        if(node==null||node instanceof NullNode)
            return null;
        else
            return t.fromJson(node);
    }

    private void toBson(BasicDBObject dest,
                        SimpleField fieldMd,
                        Path path,
                        JsonNode node) {
        Object value=toValue(fieldMd.getType(),node);
        // Should we add fields with null values to the bson doc? 
        if(value!=null) {
            logger.debug("{} = {}",path,value);
            dest.append(path.tail(0),value);
        }
    }

    /**
     * @param cursor The cursor, pointing to the first element of the object
     */
    private BasicDBObject objectToBson(JsonNodeCursor cursor,EntityMetadata md) {
        BasicDBObject ret=new BasicDBObject();
        do {
            Path path=cursor.getCurrentPath();
            JsonNode node=cursor.getCurrentNode();
            logger.debug("field: {}",path);
            FieldTreeNode fieldMdNode=md.resolve(path);
            if(fieldMdNode==null)
                throw Error.get(ERR_INVALID_FIELD,path.toString());
            
            if(fieldMdNode instanceof SimpleField) {
                toBson(ret,(SimpleField)fieldMdNode,path,node);
            } else if(fieldMdNode instanceof ObjectField) {
                if(node!=null) {
                    if(node instanceof ObjectNode) {
                        if(cursor.firstChild()) {
                            ret.append(path.tail(0),objectToBson(cursor,md));
                            cursor.parent();
                        } 
                    } else
                        throw Error.get(ERR_INVALID_FIELD,path.toString());
                }
            } else if(fieldMdNode instanceof ArrayField) {
                if(node!=null) {
                    if(node instanceof ArrayNode) {
                        if(cursor.firstChild()) {
                            ret.append(path.tail(0),arrayToBson(cursor,((ArrayField)fieldMdNode).getElement(),md));
                            cursor.parent();
                        } 
                    } else
                        throw Error.get(ERR_INVALID_FIELD,path.toString());
                }
            } else if(fieldMdNode instanceof ReferenceField) {
                //toBson(ret,(ReferenceNode)fieldMdNode,path,node);
            }            
        } while(cursor.nextSibling());
        return ret;
    }

    /**
     * @param cursor The cursor, pointing to the first element of the array
     */
    private List arrayToBson(JsonNodeCursor cursor,ArrayElement el,EntityMetadata md) {
        List l=new ArrayList();
        if(el instanceof SimpleArrayElement) {
            Type t=el.getType();
            do {
                Object value=toValue(t,cursor.getCurrentNode());
                l.add(value);
            } while(cursor.nextSibling());
        } else {
            do {
                JsonNode node=cursor.getCurrentNode();
                if(node==null||node instanceof NullNode)
                    l.add(null);
                else {
                    if(cursor.firstChild())  {
                        l.add(objectToBson(cursor,md));
                        cursor.parent();
                    } else
                        l.add(null);
                }
            } while(cursor.nextSibling());
        }
        return l;
    }
        
}
