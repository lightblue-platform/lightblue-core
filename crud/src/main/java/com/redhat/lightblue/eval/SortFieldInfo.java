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
package com.redhat.lightblue.eval;

import java.util.List;

import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.CompositeSortKey;
import com.redhat.lightblue.query.SortKey;
import com.redhat.lightblue.util.Path;

/**
 * Keeps metadata information about a sort field.
 */
public class SortFieldInfo {
    private final SimpleField field;
    private final Path name;
    private final boolean descending;
    
    public SortFieldInfo(SimpleField field,Path name,boolean descending) {
        this.field=field;
        this.name=name;
        this.descending=descending;
    }

    /**
     * Metadata for the sort field
     */
    public SimpleField getField() {
        return field;
    }
    
    /**
     * Field name
     */
    public Path getName() {
        return name;
    }

    /**
     * If sort key is descending or not
     */
    public boolean isDescending() {
        return descending;
    }

    /**
     * Build sort field info
     */
    public static SortFieldInfo[] buildSortFields(Sort sort,EntityMetadata md) {
        return buildSortFields(sort,md.getFieldTreeRoot());
    }
    
    /**
     * Build sort field info starting from the given metadata context
     */
    public static SortFieldInfo[] buildSortFields(Sort sort,FieldTreeNode context) {
        if(sort instanceof SortKey) {
            return new SortFieldInfo[] {getSortField(((SortKey)sort).getField(),context,((SortKey)sort).isDesc())};
        } else {
            List<SortKey> keys=((CompositeSortKey)sort).getKeys();
            SortFieldInfo[] arr=new SortFieldInfo[ keys.size() ];
            int i=0;
            for(SortKey key:keys) {
                arr[i]=getSortField(key.getField(),context,key.isDesc());
            }
            return arr;
        }
    }
    
    private static SortFieldInfo getSortField(Path field,FieldTreeNode context,boolean descending) {
        FieldTreeNode fieldMd=context.resolve(field);
        if(! (fieldMd instanceof SimpleField) ) {
            throw new EvaluationError(CrudConstants.ERR_EXPECTED_VALUE+":"+field);
        }
        return new SortFieldInfo((SimpleField)fieldMd,field,descending);
    }
}
