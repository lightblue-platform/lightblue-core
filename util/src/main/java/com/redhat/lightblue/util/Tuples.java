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
package com.redhat.lightblue.util;

import java.util.*;

/**
 * Provides a way to iterate through all possible n-tuples of a cartesian
 * product of several collections. When constructed with collections
 * <pre>
 *    coll_1, coll_2, coll_3, ...
 * </pre> iterates through n-tuples of the form
 * <pre>
 *     x_{1,i}, x_{2,j}, x_{3,k},...
 * </pre> where x_{m,n} denotes the n'th element of collection m.
 */
public class Tuples<T> {

    private final ArrayList<Iterable<T>> collections = new ArrayList<>();

    /**
     * Default ctor. Creates an empty tuples object that can subsequently be
     * populated with calls to <code>add</code>
     */
    public Tuples() {
    }

    /**
     * Creates a Tuples object with the given collections. The results will be
     * n-tuples, where each element i of the resulting tuple is from the i'th
     * collection.
     */
    public Tuples(Collection<T>... coll) {
        for (Collection<T> c : coll) {
            add(c);
        }
    }

    /**
     * Creates a Tuples object with the given collections. The results will be
     * n-tuples, where each element i of the resulting tuple is from the i'th
     * collection.
     */
    public Tuples(List<Collection<T>> collections) {
        for (Collection<T> c : collections) {
            add(c);
        }
    }

    /**
     * Adds a new collection to the tuples
     */
    public void add(Iterable<T> c) {
        collections.add(c);
    }

    /**
     * Returns an iterator that iterates through the cartesian product of all
     * the collections in the Tuples object.
     *
     * @return An iterator where each element is an n-tuple. The i'th element of
     * the tuple is an element from the i'th collection of the tuple.
     */
    public Iterator<List<T>> tuples() {
        return new TupleItr<>(collections);
    }

    private static final class TupleItr<X> implements Iterator<List<X>> {
        private final List<Iterable<X>> coll;
        private final List<Iterator<X>> itrList;
        private final List<X> tuple;
        private Boolean nextExists = null;
        private boolean first = true;

        public TupleItr(List<Iterable<X>> list) {
            coll = new ArrayList<>(list);
            tuple = new ArrayList<>(Collections.nCopies(coll.size(), (X) null));
            itrList = new ArrayList<>(Collections.nCopies(coll.size(), (Iterator<X>) null));
        }

        @Override
        public List<X> next() {
            if (nextExists == null) {
                nextExists = seekNext();
            }
            if (nextExists) {
                nextExists = null;
                return new ArrayList<X>(tuple);
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean hasNext() {
            if (nextExists == null) {
                nextExists = seekNext();
            }
            return nextExists;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Seeks to next available data (tuple) and constructs that result list.
         *
         * @return true if next values exist, else false
         */
        private boolean seekNext() {
            int itrLength = itrList.size();

            if (first) {
                first = false;
                for (int i = 0; i < itrLength; i++) {
                    Iterator<X> itr = coll.get(i).iterator();
                    if (itr.hasNext()) {
                        itrList.set(i, itr);
                        tuple.set(i, itr.next());
                    } else {
                        return false;
                    }
                }
                return true;
            } else {
                for (int i = itrLength - 1; i >= 0; i--) {
                    Iterator<X> itr = itrList.get(i);
                    if (itr.hasNext()) {
                        tuple.set(i, itr.next());
                        return true;
                    } else {
                        itr = coll.get(i).iterator();
                        itrList.set(i, itr);
                        tuple.set(i, itr.next());
                    }
                }
            }
            return false;
        }
    }
}
