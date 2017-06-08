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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.redhat.lightblue.util.Tuples;

public class CompositeLookupSpec extends LookupSpec {
    final LookupSpec[] values;
    
    public CompositeLookupSpec(LookupSpec[] values) {
        this.values=values;
    }
    
    @Override
    public Key buildKey() {

        List<Key> keys = new ArrayList<>();

        for(int i=0;i<values.length;i++) {
            addKeyFlat(values[i].buildKey(), keys);
        }
        return new ArrayKey(keys.toArray(new Key[keys.size()]));
    }

    /**
     *
     * @param key if ArrayKey, add it's elements instead of the key itself to prevent nesting
     * @param keys a list of keys to add to
     */
    private void addKeyFlat(Key key, List<Key> keys) {
        if (key instanceof ArrayKey) {
            keys.addAll(Arrays.asList(((ArrayKey)key).values));

        } else {
            keys.add(key);
        }
    }
    
    @Override
    public boolean matches(Key k) {
        if(k instanceof ArrayKey) {
            for(int i=0;i<values.length;i++) {
                if(!values[i].matches( ((ArrayKey)k).values[i]))
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    protected boolean needsScan() {
        for(LookupSpec lv:values)
            if(lv.needsScan())
                return true;
        return false;
    }
    
    @Override
    protected boolean multiValued() {
        for(LookupSpec lv:values)
            if(lv.multiValued())
                return true;
        return false;
    }
    
    @Override
    protected boolean iterate(Tuples<Object> tuples) {
        boolean ret=false;
        for(LookupSpec lv:values)
            if(lv.iterate(tuples))
                ret=true;
        return ret;
    }
    
    @Override
    protected LookupSpec next(Iterator<Object> tuple) {
        LookupSpec[] newValues=new LookupSpec[values.length];
        for(int i=0;i<values.length;i++) {
            newValues[i]=values[i].next(tuple);
        }
        return new CompositeLookupSpec(newValues);
    }
}
