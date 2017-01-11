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
package com.redhat.lightblue.mindex;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.assoc.QueryFieldInfo;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Tuples;
import com.redhat.lightblue.util.KeyValueCursor;

/**
 * An array key spec includes an array of KeySpecs, one for each field. Each can be a SimpleKeySpec or ArrayKeySpec
 * This is used for several fields under an array. Any nested key specs are relative to the array field
 */
public class ArrayKeySpec implements KeySpec,Comparator<ArrayKey> {
    final KeySpec[] keyFields;
    final Path arrayName;
    
    public ArrayKeySpec(QueryFieldInfo array,KeySpec[] fields) {
        this.keyFields=fields;
        this.arrayName=array.getEntityRelativeFieldNameWithContext();
    }

    @Override
    public int compareKeys(Key k1,Key k2) {
        return compare( (ArrayKey)k1,(ArrayKey)k2);
    }
    
    @Override
    public int compare(ArrayKey v1,ArrayKey v2) {
        for(int i=0;i<keyFields.length;i++) {
            int result=keyFields[i].compareKeys(v1.values[i],v2.values[i]);
            if(result!=0)
                return result;
        }
        return 0;
    }
    
    @Override
    public Set<Key> extract(JsonDoc doc,Set<Key> set) {
        if(set==null)
            set=new HashSet<>();
        KeyValueCursor<Path,JsonNode> arrayCursor=doc.getAllNodes(arrayName);
        while(arrayCursor.hasNext()) {
            arrayCursor.next();
            ArrayNode arrayNode=(ArrayNode)arrayCursor.getCurrentValue();
            for(Iterator<JsonNode> itr=arrayNode.elements();itr.hasNext();) {
                JsonNode element=itr.next();
                JsonDoc nestedDoc=new JsonDoc(element);
                Tuples<Key> tuples=new Tuples<>();
                for(KeySpec keyField:keyFields) {
                    tuples.add(keyField.extract(nestedDoc,null));
                }
                Iterator<List<Key>> tupleItr=tuples.tuples();
                while(tupleItr.hasNext()) {
                    List<Key> t=tupleItr.next();
                    set.add(new ArrayKey(t.toArray(new Key[t.size()])));
                }
            }
        }
        return set;
    }
}
