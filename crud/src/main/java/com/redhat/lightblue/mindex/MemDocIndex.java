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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Comparator;
import java.util.List;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Tuples;

/**
 * This is an in-memory document index implementation using a map.
 *
 * Before using the index, the caller should decide what type of
 * queries will be run on the index, and build a key spec to be used
 * for index keys. The index is declare dusing a KeySpec. A KeySpec
 * can be a SimpleKeySpec for a top-level value field:
 *
 * <pre>
 *    { field: f1, op:=, rvalue:str } -> 
 *          new SimpleKeySpec(md.resolve("f1"))
 * </pre>
 *
 * Or a simple value field under an array:
 * 
 * <pre>
 *   { field: arr.*.f1, op:=, rvalue:str} -> 
 *         new SimpleKeySpec(md.resolve("arr.*.f1"))
 * </pre>
 *
 * If the field is accessed using an elem-match search, and there are
 * multiple predicates involved under the same array, then an
 * ArrayKeySpec must be used:
 * 
 * <pre>
 * { array:arr, elemMatch: { $and: [ {field:f1,op:=,rvalue:str1}, {field:f2,op:=,rvalue:str2}]}} ->
 *         arrMd=md.resolve("arr")
 *         new ArrayKeySpec(arrMd,new KeySpec[] {
 *                new SimpleKeySpec(arrMd.getElement().resolve("f1")),
 *                new SimpleKeySpec(arrMd.getElement().resolve("f2"))});
 * </pre>
 * 
 * For clauses combined with AND, use CompositeKeySpec.
 *
 * Once the index is constructed with a key spec, add docs using add()
 * method. This will create index entries for each doc using the
 * keyspec.
 *
 * For lookups, the caller must create a lookup spec in the same
 * structure as the key spec. A lookup spec composed ot only Value
 * lookups and multi-value lookups is a simple lookup. If a range
 * lookup spec or prefix lookup spec is used, the lookup becomes an
 * index scan.
 *    
 */
public class MemDocIndex {
    
    /**
     * The index keys are ordered based on the keyFields array
     */
    private final HashMap<Key,Set<JsonDoc>> documents;   
    public final KeySpec keySpec;
    
    /**
     * Constructs a document index using the given key spec
     */
    public MemDocIndex(KeySpec keys) {
        this.keySpec=keys;
        this.documents=new HashMap<Key,Set<JsonDoc>>();
    }
    
    /**
     * Clear the indexed documents
     */
    public void clear() {
        documents.clear();
    }

    /**
     * Add the document to the index
     */
    public void add(JsonDoc doc) {
        Set<Key> keys=keySpec.extract(doc,null);
        for(Key k:keys) {
            Set<JsonDoc> docSet=documents.get(k);
            if(docSet==null)
                documents.put(k,docSet=new HashSet<>());
            docSet.add(doc);
        }
    }

    public Set<JsonDoc> find(LookupSpec spec) {
        Set<JsonDoc> results=new HashSet<>();
        if(spec.multiValued()) {
            Tuples<Object> tuples=new Tuples<>();
            spec.iterate(tuples);
            for(Iterator<List<Object>> itr=tuples.tuples();itr.hasNext();) {
                List<Object> tuple=itr.next();
                LookupSpec singleValueSpec=spec.next(tuple.iterator());
                findSingleValue(singleValueSpec,results);
            }
        } else {
            findSingleValue(spec,results);
        }
        return results;
    }

    private void findSingleValue(LookupSpec spec,Set<JsonDoc> results) {
        if(spec.needsScan()) {
            indexScan(spec,results);
        } else {
            indexLookup(spec,results);
        }
    }

    private void indexScan(LookupSpec spec,Set<JsonDoc> results) {
        for(Map.Entry<Key,Set<JsonDoc>> entry:documents.entrySet()) {
            Key indexKey=entry.getKey();
            if(spec.matches(indexKey))
                results.addAll(entry.getValue());
        }
    }

    private void indexLookup(LookupSpec spec,Set<JsonDoc> results) {
        Set<JsonDoc> docs=documents.get(spec.buildKey());
        if(docs!=null)
            results.addAll(docs);
    }
    
    @Override
    public String toString() {
        StringBuilder bld=new StringBuilder();
        for(Map.Entry<Key,Set<JsonDoc>> entry:documents.entrySet()) {
            bld.append(entry.getKey()).append(":").append("[");
            for(JsonDoc d:entry.getValue()) {
                bld.append(' ');
                bld.append(Integer.toHexString(d.hashCode()));
            }
            bld.append("]\n");
        }
        return bld.toString();
    }
}
