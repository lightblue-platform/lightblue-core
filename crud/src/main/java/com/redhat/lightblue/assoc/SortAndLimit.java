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
package com.redhat.lightblue.assoc;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.eval.SortFieldInfo;
import com.redhat.lightblue.eval.SortableItem;

import com.redhat.lightblue.util.JsonDoc;

/**
 * When associations are evaluated with a query plan that is different from the
 * entity structure, the root entity cannot be sorted or limited. This class
 * gets the result sets, sorts it, and applies the limit to it.
 */
public class SortAndLimit {
    private final SortFieldInfo[] sortFields;
    private final Long from;
    private final Long to;

    private static class ResultDocSortableItem extends SortableItem {

        private final ResultDoc doc;

        public ResultDocSortableItem(ResultDoc doc, SortFieldInfo[] sortFields) {
            super(doc.getDoc().getRoot(), sortFields);
            this.doc = doc;
        }
    }

    public SortAndLimit(EntityMetadata md, Sort sort, Long from, Long to) {
        if (sort != null) {
            sortFields = SortFieldInfo.buildSortFields(sort, md);
        } else {
            sortFields = null;
        }
        this.from = from;
        this.to = to;
    }

    /**
     * Sort and limit the resultset
     */
    public List<ResultDoc> process(List<ResultDoc> docs) {
        List<ResultDoc> resultList;
        if (sortFields != null) {
            List<ResultDocSortableItem> sortList = new ArrayList<>(docs.size());
            for (ResultDoc doc : docs) {
                sortList.add(new ResultDocSortableItem(doc, sortFields));
            }
            Collections.sort(sortList);
            resultList = new ArrayList<>(docs.size());
            for (ResultDocSortableItem item : sortList) {
                resultList.add(item.doc);
            }
        } else {
            resultList = docs;
        }

        int size = resultList.size();
        int f = from == null ? 0 : from.intValue();
        if (f >= size) {
            return new ArrayList<>();
        }

        int t = to == null ? size - 1 : to.intValue();
        if (t >= size) {
            t = size - 1;
        }
        return resultList.subList(f, t + 1);
    }
}
