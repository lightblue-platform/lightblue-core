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
package com.redhat.lightblue.crud;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Marker interface for requests containing if-same flag, and doc versions
 */
public interface WithIfCurrent {

    /**
     * Returns true if the is-samne-only flag is set.
     */
    boolean isIfCurrentOnly();
    void setIfCurrentOnly(boolean b);

    /**
     * The list of document versions. If isIfCurrentOnly()==true, then,
     * only the documents that are in this list will be updated, and
     * only if their versions are the same.
     */
    List<String> getDocumentVersions();
    void setDocumentVersions(List<String> s);

    public static void toJson(WithIfCurrent w,ObjectNode parent) {
        if(w!=null&&w.isIfCurrentOnly()) {            
            parent.set("onlyIfCurrent",JsonNodeFactory.instance.booleanNode(true));
            List<String> versions=w.getDocumentVersions();
            if(versions!=null&&!versions.isEmpty()) {
                ArrayNode arr=JsonNodeFactory.instance.arrayNode();
                for(String x:versions)
                    arr.add(JsonNodeFactory.instance.textNode(x));
                parent.set("documentVersions",arr);
            }
        }
    }

    public static void fromJson(WithIfCurrent dest,ObjectNode node) {
        JsonNode x=node.get("onlyIfCurrent");
        if(x instanceof ValueNode && x.booleanValue()) {
            dest.setIfCurrentOnly(true);
            x=node.get("documentVersions");
            if(x instanceof ArrayNode) {
                List<String> versions=new ArrayList<>(x.size());
                for(Iterator<JsonNode> itr=x.elements();itr.hasNext();) {
                    JsonNode elem=itr.next();
                    if(!(elem instanceof NullNode)) {
                        versions.add(elem.asText());
                    }
                }
                dest.setDocumentVersions(versions);
            }
        } else {
            dest.setIfCurrentOnly(false);
        }
    }
}
