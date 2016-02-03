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

import com.redhat.lightblue.util.Path;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author nmalik
 */
public class Indexes implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ArrayList<Index> indexes = new ArrayList<>();

    public Indexes() {
    }

    public Indexes(Index... ix) {
        for (Index x : ix) {
            indexes.add(x);
        }
    }

    public void add(Index x) {
        indexes.add(x);
    }

    public void setIndexes(Collection<Index> indexes) {
        this.indexes.clear();
        if (indexes != null) {
            this.indexes.addAll(indexes);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Index> getIndexes() {
        return (List<Index>) indexes.clone();
    }

    public boolean isEmpty() {
        return indexes.isEmpty();
    }

    public boolean isCaseInsensitiveKey(Path path) {
        return getCaseInsensitiveIndexes().stream()
                .anyMatch(i -> i.getField().equals(path));

    }

    public List<IndexSortKey> getCaseInsensitiveIndexes() {
        return this.indexes.stream()
                .map(Index::getFields)
                .flatMap(Collection::stream)
                .filter(IndexSortKey::isCaseInsensitive)
                .collect(Collectors.toList());
    }

    /**
     * Returns the indexes that can be used to evaluate a search criteria
     * containing the given fields
     *
     * @param fields List of fields for which a search will be conducted
     *
     * @return A map of index -> set<Path> where for each index, the mapped path
     * set gives the fields that can be searched efficiently using that index.
     */
    public Map<Index, Set<Path>> getUsefulIndexes(Collection<Path> fields) {
        Map<Index, Set<Path>> m = new HashMap<>();
        for (Index ix : indexes) {
            Set<Path> u = ix.getUsefulness(fields);
            if (!u.isEmpty()) {
                m.put(ix, u);
            }
        }
        return m;
    }
}
