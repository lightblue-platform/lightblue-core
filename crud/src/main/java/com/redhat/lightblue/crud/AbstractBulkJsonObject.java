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
import java.util.Collections;
import java.util.Comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.JsonObject;
import com.redhat.lightblue.Request;

/**
 * Base class for bulk request and responses. This class contains the
 * code common to both, as bulk request and response are structurally
 * similar. It deals with a JSON of the form:
 *
 *
 * <pre>
 *   {
 *     "<entries>": [
 *         {
 *             "seq":0,
 *             "op": "FIND",
 *             "<entry>": { item }
 *         }
 *     ]
 *   }
 * </pre> 
 */
abstract class AbstractBulkJsonObject<T extends JsonObject> extends JsonObject {

    protected final List<T> entries=new ArrayList<T>();

    /**
     * Returns all entries in the bulk object
     */
    public List<T> getEntries() {
        return entries;
    }

    /**
     * Sets the entries in the bulk object. Copies the collection
     */
    public void setEntries(List<T> x) {
        entries.clear();
        entries.addAll(x);
    }

    public void add(T x) {
        entries.add(x);
    }

    /**
     * Overriding this method allows the concrete class to add any necessary items to the entry node.
     *
     * @param node The object node, an element of the entries array
     * @param entry The entry
     */
    protected abstract void toJsonEntryNode(ObjectNode node,T entry);

    /**
     * Returns a JSON representation of this
     */
    public JsonNode toJson(String entriesName, String entryName) {
        JsonNodeFactory factory=getFactory();
        ObjectNode node = factory.objectNode();
        ArrayNode arr=factory.arrayNode();
        int seq=0;
        for(T x:entries) {
            ObjectNode entryNode=factory.objectNode();
            entryNode.set("seq",factory.numberNode(seq++));
            toJsonEntryNode(entryNode,x);
            entryNode.set(entryName,x.toJson());
            arr.add(entryNode);
        }
        node.put(entriesName, arr);
        return node;
    }

    private static class SReq {
        private final int seq;
        private final JsonObject entry;

        public SReq(int seq,JsonObject entry) {
            this.seq=seq;
            this.entry=entry;
        }
    }

    /**
     * Parses the actual entry from the given node
     *
     * @param entry The entry object, an element of the entries array
     */
    protected abstract T parseEntry(ObjectNode entry);
    
    /**
     * Parses the bulk object from the given array node
     */
    protected void parse(ArrayNode entriesArray) {
        entries.clear();
        // Fill the entries into an array list, assuming for most
        // cases the array list will be ordered by the sequence
        // number. If there are out-of-sequence entries, then sort it
        List<SReq> list=new ArrayList<>(entriesArray.size());
        int lastSeq=0;
        boolean first=true;
        boolean ooo=false;
        for(Iterator<JsonNode> itr=entriesArray.elements();itr.hasNext();) {
            JsonNode x=itr.next();
            JsonNode val=x.get("seq");
            if(val!=null) {
                int seq=val.asInt();
                if(first)
                    lastSeq=seq;
                else {
                    if(seq<lastSeq)
                        ooo=true;
                    else
                        lastSeq=seq;
                }
                T entry=parseEntry((ObjectNode)x);
                list.add(new SReq(seq,entry));
            }
        }
        if(ooo)
            Collections.sort(list,new Comparator<SReq>() {
                    public int compare(SReq r1,SReq r2) {
                        return r1.seq-r2.seq;
                    }
                });
        for(SReq r:list)
            entries.add((T)r.entry);
    }
}
