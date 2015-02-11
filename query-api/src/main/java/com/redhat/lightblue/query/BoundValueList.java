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
package com.redhat.lightblue.query;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A modifiable list of values. When rfield in a
 * NaryFieldRelationalExpression is bound to a value, that expression
 * is rewritten as a NaryValueRelationalExpression with a
 * BoundListValue, so the caller can change the underlying list for
 * every execution.
 */
public class BoundValueList implements List<Value> {

    private List<Value> list;

    /**
     * Creates a BoundValueList with list=null
     */
    public BoundValueList() {
        this(null);
    }

    /**
     * Creates a BoundValueList with list=l
     */
    public BoundValueList(List<Value> l) {
        this.list=l;
    }

    /**
     * Sets the list
     */
    public void setList(List<Value> l) {
        this.list=l;
    }

    @Override
    public boolean add(Value v) {
        return list.add(v);
    }

    @Override
    public void add(int index,Value element) {
        list.add(index,element);
    }

    @Override
    public boolean addAll(Collection<? extends Value> x) {
        return list.addAll(x);
    }

    @Override
    public boolean addAll(int i,Collection<? extends Value> x) {
        return list.addAll(i,x);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> x) {
        return list.containsAll(x);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof BoundValueList)
            return list.equals(((BoundValueList)o).list);
        else
            return list.equals(o);
    }

    @Override
    public Value get(int index) {
        return list.get(index);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<Value> iterator() {
        return list.iterator();
    }

    @Override
    public int lastIndexOf(Object v) {
        return list.lastIndexOf(v);
    }

    @Override
    public ListIterator<Value> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<Value> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public Value remove(int index) {
        return list.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public Value set(int index,Value e) {
        return list.set(index,e);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public List<Value> subList(int fromIndex,int toIndex) {
        return list.subList(fromIndex,toIndex);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }
}
