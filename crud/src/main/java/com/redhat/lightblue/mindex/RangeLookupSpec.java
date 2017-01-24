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

import java.util.Iterator;

import com.redhat.lightblue.util.Tuples;

public class RangeLookupSpec extends SimpleKeyLookupSpec {
    final Object from,to;
    
    public RangeLookupSpec(SimpleKeySpec keyField,Object from,Object to) {
        super(keyField);
        this.from=keyField.type.cast(from);
        this.to=keyField.type.cast(to);
    }
    
    @Override
    public boolean matches(Key k) {
        if(k instanceof SimpleKey) {
            return key.type.compare( from, ((SimpleKey)k).value) <=00 &&
                key.type.compare( to, ((SimpleKey)k).value) >=0;
        } else {
            return false;
        }
    }
    
    @Override protected boolean needsScan() {return true;}
    @Override protected boolean multiValued() {return false;}
    @Override protected boolean iterate(Tuples<Object> tuples) {return false;}
    @Override protected LookupSpec next(Iterator<Object> tuple) {return this;}
}

