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

import java.util.Objects;

/**
 * SimpleKey wraps a single value
 */
class SimpleKey implements Key {
    final Object value;
    int hcode;
    boolean hcodeInitialized;

    static final SimpleKey NULL_KEY=new SimpleKey(null);
    
    SimpleKey(Object v) {
        value=v;
    }
    
    @Override
    public int hashCode() {
        if(!hcodeInitialized) {
            hcode=Objects.hashCode(value);
            hcodeInitialized=true;
        }
        return hcode;
    }
    
    @Override
    public boolean equals(Object o) {
        if(hashCode()==o.hashCode()) {
            return Objects.equals( value, ((SimpleKey)o).value );
        }
        return false;
    }
    
    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
