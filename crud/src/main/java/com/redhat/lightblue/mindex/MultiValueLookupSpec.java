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
import java.util.Collection;

import com.redhat.lightblue.util.Tuples;

/**
 * Lookup for some values of a simple key
 */
public class MultiValueLookupSpec extends SimpleKeyLookupSpec {
    final Set<Object> values;
    
    public MultiValueLookupSpec(SimpleKeySpec keyField,Collection<Object> values) {
        super(keyField);
        this.values=new HashSet<Object>();
        for(Object x:values)
            this.values.add(keyField.type.cast(x));
    }
    
    @Override
    public boolean matches(Key k) {
        if(k instanceof SimpleKey) {
            return values.contains( ((SimpleKey)k).value);
        } else {
            return false;
        }
    }
    
    @Override protected boolean needsScan() {return false;}
    @Override protected boolean multiValued() {return values.size()>1;}
    
    @Override
    protected boolean iterate(Tuples<Object> tuples) {
        if(values.size()>1) {
            tuples.add(values);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    protected LookupSpec next(Iterator<Object> tuple) {
        if(multiValued()) {
            return new ValueLookupSpec(key,tuple.next());
        } else {
            return this;
        }
    }
}
