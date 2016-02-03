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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.redhat.lightblue.util.Path;

/**
 * Specifies that the combined value of one or more fields must be unique
 */
public class Index implements Serializable {

    private static final long serialVersionUID = 1l;

    private String name;
    private boolean unique = false;
    private final ArrayList<IndexSortKey> fields = new ArrayList<>();

    /**
     * Default ctor
     */
    public Index() {
    }

    public Index(IndexSortKey... f) {
        for (IndexSortKey k : f) {
            fields.add(k);
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the unique
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * @param unique the unique to set
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * The fields such that the ordered combination of their values must be
     * unique
     */
    @SuppressWarnings("unchecked")
    public List<IndexSortKey> getFields() {
        return (ArrayList<IndexSortKey>) fields.clone();
    }

    /**
     * The fields such that the ordered combination of their values must be
     * unique
     */
    public void setFields(List<IndexSortKey> f) {
        fields.clear();
        if (f != null) {
            fields.addAll(f);
        }
    }

    /*
     * Return the set of fields for which this index can be useful
     * during a search involving those fields.
     */
    public Set<Path> getUsefulness(Collection<Path> searchFields) {
        Set<Path> ret = new HashSet<>();
        for (IndexSortKey key : fields) {
            boolean found = false;
            for (Path searchField : searchFields) {
                if (key.getField().equals(searchField)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                ret.add(key.getField());
            } else {
                break;
            }
        }
        return ret;
    }

    /**
     * Returns if this index can be useful to search the given field
     */
    public boolean isUseful(Path field) {
        for (IndexSortKey k : fields) {
            if (k.getField().equals(field)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCaseInsensitiveKey(Path path) {
        return fields.stream()
            .filter(IndexSortKey::isCaseInsensitive)
            .anyMatch(i -> i.getField().equals(path));
    }
}
