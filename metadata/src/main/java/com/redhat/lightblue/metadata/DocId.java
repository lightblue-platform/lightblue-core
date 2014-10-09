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
package com.redhat.lightblue.metadata;

import java.io.Serializable;

/**
 * This class is used to represent document identities. It
 * encapsulates an array of values that represent the values of the
 * identity fields of a document, and the document type.
 */
public final class DocId implements Serializable {

    private static final long serialVersionUID=1l;
    
    private final Object[] values;

    public DocId(Object[] values) {
        this.values=values;
    }

    @Override
    public int hashCode() {
        int value=1;
        for(Object x:values)
            if(x!=null)
                value*=x.hashCode();
        return value;
    }

    @Override
    public boolean equals(Object x) {
        if(x instanceof DocId) {
            DocId d=(DocId)x;
            if(d.values.length==values.length) {
                for(int i=0;i<values.length;i++)
                    if( !(values[i]==null&&d.values[i]==null)&&
                        !(values[i]!=null&&d.values[i]!=null&&d.values[i].equals(values[i])) )
                        return false;
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder bld=new StringBuilder();
        for(int i=0;i<values.length;i++) {
            if(i>0)
                bld.append(':');
            bld.append(values.toString());
        }
        return bld.toString();
    }
}
