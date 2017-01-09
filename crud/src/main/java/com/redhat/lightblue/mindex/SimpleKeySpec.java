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

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;

/**
 * Simple key specification, includes a single field
 */
public class SimpleKeySpec implements KeySpec,Comparator<SimpleKey> {
    final FieldTreeNode fieldMd;
    final Path fullName;
    final Type type;
    
    public SimpleKeySpec(FieldTreeNode fieldMd) {
        this.fieldMd=fieldMd;
        this.fullName=fieldMd.getFullPath();
        this.type=fieldMd.getType();
    }
    
    @Override
    public int compareKeys(Key k1,Key k2) {
        return compare( (SimpleKey)k1,(SimpleKey)k2);
    }
    
    @Override
    public int compare(SimpleKey v1,SimpleKey v2) {
        return type.compare(v1.value,v2.value);
    }
    
    @Override
    public Set<Key> extract(JsonDoc doc) {
        HashSet<Key> set=new HashSet<>();
        KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(fullName);
        while(cursor.hasNext()) {
            cursor.next();
            set.add(new SimpleKey(type.fromJson(cursor.getCurrentValue())));
        }
        if(set.isEmpty()) {
        	// No value in doc: insert null
        	set.add(new SimpleKey(null));
        }
        return set;
    }
}
