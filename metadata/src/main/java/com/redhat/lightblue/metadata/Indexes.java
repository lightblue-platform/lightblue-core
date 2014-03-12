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
import java.util.List;

/**
 *
 * @author nmalik
 */
public class Indexes implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ArrayList<Index> indexes = new ArrayList<>();

    public Indexes() {
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
}
