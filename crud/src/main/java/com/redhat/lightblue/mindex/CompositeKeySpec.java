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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Tuples;

/**
 * Composite key specification, includes multiple fields. Uses ArrayKey as the key type
 */
public class CompositeKeySpec implements KeySpec,Comparator<ArrayKey> {
    final KeySpec[] keyFields;
    
    public CompositeKeySpec(KeySpec[] keyFields) {
        this.keyFields=keyFields;
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
        Tuples<Key> tuples=new Tuples<>();
        for(int i=0;i<keyFields.length;i++) {
            tuples.add(keyFields[i].extract(doc,null));
        }
        for(Iterator<List<Key>> itr=tuples.tuples();itr.hasNext();) {
            List<Key> l=itr.next();
            set.add(new ArrayKey(l.toArray(new Key[l.size()])));
        }
        return set;
    }
}
