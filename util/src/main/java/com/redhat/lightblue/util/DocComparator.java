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

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;

/**
 * Compares two documents represented as an object tree, and builds an list of
 * all changes
 *
 * Containers (objects and arrays) are compared recursively. The comparison
 * algorithm works like this:
 *
 * Objects: A field-by-field comparison is done. If a field exists in the first
 * document but not in the second, that field is removed. If a field exists in
 * the second document but not the first, that field is added. If a field exists
 * in both documents with different values, that field is modified.
 *
 * Arrays: There are two possible algorithms to compare arrays. If array
 * elements contain a unique identifier (which is defined by the caller), then
 * array elelements of the first and the second document are matched using the
 * unique identifiers of array elements. Then each matching array element is
 * compared to generate the detailed difference. If array elements don't have
 * unique identifiers, then each element of the first array is compared to each
 * element of the second array, and the elements with minimal number of changes
 * are associated. Elements that are too different from each other are not
 * associated.
 *
 * Differences:
 *
 * An Addition denotes a new field or array element. Addition.field1 is null,
 * meaning the field does not exist in document1, and Addition.field2 denotes
 * the new field, or array element.
 *
 * A Removal denotes a removed field or array element. Removal.field1 denotes
 * the element in document1, and Removal.field2 is null.
 *
 * A Modification denotes a content modification of a field, or array element.
 * Both field1 and field2 are non-null, and set to the name of the modified
 * field.
 *
 * A Move denotes an array element move. field1 denotes the old index of the
 * array element, and field2 denotes the new index.
 *
 * If new elements are added to an array, or existing elements are removed, the
 * addition and removal appear as diff, and any node that shifted during the
 * operation appears within a Move.
 */
public abstract class DocComparator<BaseType, ValueType, ObjectType, ArrayType> {

    /**
     * Thrown if there is an array whose elements contain identities, but for at
     * least one element identity cannot be retrieved
     */
    public static final class InvalidArrayIdentity extends Exception {
        private Path path;
        public InvalidArrayIdentity(Path p) {
            super(p.toString());
            this.path=p;
        }
        
        public Path getPath() {
            return path;
        }
     }

    /**
     * Thrown if there is an array whose elements contain identities, but they
     * are not unique
     */
    public static final class DuplicateArrayIdentity extends Exception {
        private Path path;
        public DuplicateArrayIdentity(Path p) {
            super(p.toString());
            this.path=p;
        }

        public Path getPath() {
            return path;
        }
    }

    /**
     * The array element identity extractor interface.
     */
    public interface IdentityExtractor<T> {

        /**
         * It should return an identity object from the given array element. The
         * returned object should implement equals and hashCode methods.
         */
        Object getIdentity(T element);
    }

    /**
     * Contains the edit script, and number of changed and unchanged fields. The
     * number of changed fields does not include array element moves, it only
     * includes additions, removals, and modifications.
     */
    public static class Difference<T> {
        private final List<Delta<T>> delta;
        private int numUnchangedFields;
        private int numChangedFields;

        /**
         * Default ctor, sets numUnchangedFields to zero, initializes an empty
         * list
         */
        public Difference() {
            delta = new ArrayList<>();
            numUnchangedFields = 0;
        }

        public Difference(List<Delta<T>> delta) {
            this.delta = delta;
            for (Delta d : this.delta) {
                if (d instanceof Addition
                        || d instanceof Removal
                        || d instanceof Modification) {
                    numChangedFields++;
                }
            }
        }

        /**
         * Constructs a difference with one modification, no unchanged fields
         */
        public Difference(Delta<T> d) {
            this(new ArrayList<Delta<T>>(1));
            add(d);

        }

        /**
         * Constructs a Difference denoting no difference
         */
        public Difference(int numFields) {
            delta = new ArrayList<>();
            numUnchangedFields = numFields;
        }

        /**
         * Returns the number of unmodified fields
         */
        public int getNumUnchangedFields() {
            return numUnchangedFields;
        }

        /**
         * Returns the number of modified fields, excluding array element moves
         */
        public int getNumChangedFields() {
            return numChangedFields;
        }

        /**
         * Returns the list of changes
         */
        public List<Delta<T>> getDelta() {
            return delta;
        }

        public void add(Difference<T> diff) {
            delta.addAll(diff.delta);
            numUnchangedFields += diff.numUnchangedFields;
            numChangedFields += diff.numChangedFields;
        }

        public void add(Delta<T> d) {
            delta.add(d);
            if (d instanceof Addition
                    || d instanceof Removal
                    || d instanceof Modification) {
                numChangedFields++;
            }
        }

        /**
         * A numeric value between 0 and 1 denoting how much an object is
         * changed. 0 means no change, 1 means everything is changed.
         */
        public double getChangeAmount() {
            double d = numChangedFields + numUnchangedFields;
            return d == 0 ? 0 : numChangedFields / d;
        }

        /**
         * Returns true if there are not changes
         */
        public boolean same() {
            return delta.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder bld = new StringBuilder();
            for (Delta x : delta) {
                bld.append(x.toString()).append('\n');
            }
            return bld.toString();
        }
    }

    /**
     * Base class for a delta
     */
    public static abstract class Delta<T> {
        protected final Path field1;
        protected final Path field2;

        public Delta(Path field1, Path field2) {
            this.field1 = field1 == null ? null : field1.immutableCopy();
            this.field2 = field2 == null ? null : field2.immutableCopy();
        }

        public Path getField1() {
            return field1;
        }

        public Path getField2() {
            return field2;
        }

        /**
         * Return the non-null field, or field1 if both are non-null
         */
        public Path getField() {
            return field1 == null ? field2 : field1;
        }
    }

    /**
     * Denotes an addition of a field or array element that isn't present in
     * doc1
     */
    public static class Addition<T> extends Delta<T> {
        private final T addedNode;

        public Addition(Path field2, T addedNode) {
            super(null, field2);
            this.addedNode = addedNode;
        }

        public T getAddedNode() {
            return addedNode;
        }

        @Override
        public String toString() {
            return "+ " + field2 + ":" + addedNode;
        }
    }

    /**
     * Denotes a removal of a field or array element that is present in doc1 but
     * not in doc2
     */
    public static class Removal<T> extends Delta<T> {
        private final T removedNode;

        public Removal(Path field1, T removedNode) {
            super(field1, null);
            this.removedNode = removedNode;
        }

        public T getRemovedNode() {
            return removedNode;
        }

        @Override
        public String toString() {
            return "- " + field1 + ":" + removedNode;
        }
    }

    /**
     * Denotes an array element move from one element index to another
     */
    public static class Move<T> extends Delta<T> {
        private final T movedNode;

        public Move(Path field1, Path field2, T movedNode) {
            super(field1, field2);
            this.movedNode = movedNode;
        }

        public T getMovedNode() {
            return movedNode;
        }

        @Override
        public String toString() {
            return "* " + field1 + "->" + field2 + ":" + movedNode;
        }
    }

    /**
     * Denotes a field modification
     */
    public static class Modification<T> extends Delta<T> {
        private final T node1;
        private final T node2;

        public Modification(Path field1, T node1, Path field2, T node2) {
            super(field1, field2);
            this.node1 = node1;
            this.node2 = node2;
        }

        public T getUnmodifiedNode() {
            return node1;
        }

        public T getModifiedNode() {
            return node2;
        }

        @Override
        public String toString() {
            return "* " + field1 + "->" + field2 + ":" + node1 + " -> " + node2;
        }
    }

    /**
     * Contains a list of fields contained in array elements that uniquely
     * identify array elements
     */
    public static class ArrayIdentityFields {
        private Path[] fields;

        public ArrayIdentityFields(Path... fields) {
            this.fields = fields;
        }

        public Path[] getFields() {
            return fields;
        }

        @Override
        public String toString() {
            return Arrays.toString(fields);
        }
    }

    /**
     * Default array identity object. Contains the identity values, computes
     * hashcode from them
     */
    public class DefaultIdentity {
        private final BaseType[] nodes;
        private Integer hcode;

        public DefaultIdentity(BaseType[] nodes) {
            this.nodes = nodes;
        }

        @Override
        public int hashCode() {
            if (hcode == null) {
                int code = 0;
                for (int i = 0; i < nodes.length; i++) {
                    if (nodes[i] != null) {
                        code += nodes[i].hashCode();
                    }
                }
                hcode = code;
            }
            return hcode;
        }

        @Override
        public boolean equals(Object x) {
            try {
                DefaultIdentity d = (DefaultIdentity) x;
                for (int i = 0; i < nodes.length; i++) {
                    if (!DocComparator.this.equals(asValue(d.nodes[i]), asValue(nodes[i]))) {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }

            return true;
        }
    }

    private final Map<Path, ArrayIdentityFields> arrayIdentities = new HashMap<>();

    protected abstract boolean isValue(BaseType value);

    protected abstract boolean isArray(BaseType value);

    protected abstract boolean isObject(BaseType value);

    protected abstract boolean isNull(BaseType value);

    protected abstract ValueType asValue(BaseType value);

    protected abstract ArrayType asArray(BaseType value);

    protected abstract ObjectType asObject(BaseType value);

    protected abstract boolean equals(ValueType v1, ValueType v2);

    protected abstract Iterator<Map.Entry<String, BaseType>> getFields(ObjectType o);

    protected abstract boolean hasField(ObjectType value, String field);

    protected abstract BaseType getField(ObjectType value, String field);

    protected abstract IdentityExtractor getArrayIdentityExtractorImpl(ArrayIdentityFields fields);

    protected abstract BaseType getElement(ArrayType value, int index);

    protected abstract int size(ArrayType value);

    /**
     * Adds a group of fields that can uniquely identify array elements for
     * object arrays
     *
     * @param array The name of the array field
     * @param identities The fields of the array element that can identiy an
     * element
     *
     * In the following document:      <pre>
     * {
     * ...
     *   "aField": [
     *      { "_id":1,"field":...},
     *      { "_id":2,"field":...}
     *   ]
     * }
     * <pre>
     * the call looks like
     * <pre>
     *    jsonCompare.addArrayIdentity(new Path("aField"),new Path("_id"));
     * </pre> If there are more than one fields that uniquely identify an
     * eleent, list those in the argument list.
     */
    public void addArrayIdentity(Path array, Path... identities) {
        arrayIdentities.put(array, new ArrayIdentityFields(identities));
    }

    public Map<Path, ArrayIdentityFields> getArrayIdentities() {
        return arrayIdentities;
    }

    /**
     * Compares two documents and returns the difference
     */
    public Difference<BaseType> compareNodes(BaseType node1, BaseType node2)
            throws InvalidArrayIdentity, DuplicateArrayIdentity {
        return compareNodes(new MutablePath(), node1, new MutablePath(), node2);
    }

    public Difference<BaseType> compareNodes(MutablePath field1,
                                             BaseType node1,
                                             MutablePath field2,
                                             BaseType node2)
            throws InvalidArrayIdentity, DuplicateArrayIdentity {
        if (isValue(node1) && isValue(node2)) {
            if (!equals(asValue(node1), asValue(node2))) {
                return new Difference(new Modification(field1, node1, field2, node2));
            }
        } else if (isArray(node1) && isArray(node2)) {
            return compareArrays(field1, asArray(node1), field2, asArray(node2));
        } else if (isObject(node1) && isObject(node2)) {
            return compareObjects(field1, asObject(node1), field2, asObject(node2));
        } else if (!(isNull(node1) && isNull(node2))) {
            return new Difference<>(new Modification(field1, node1, field2, node2));
        }
        return new Difference<>(1);
    }

    /**
     * Compares two object nodes recursively and returns the differences
     */
    public Difference<BaseType> compareObjects(MutablePath field1,
                                               ObjectType node1,
                                               MutablePath field2,
                                               ObjectType node2)
            throws InvalidArrayIdentity, DuplicateArrayIdentity {
        Difference<BaseType> ret = new Difference<>();
        // Field by field comparison of obj1 to obj2. 
        for (Iterator<Map.Entry<String, BaseType>> fields = getFields(node1); fields.hasNext();) {
            Map.Entry<String, BaseType> field = fields.next();
            String fieldName = field.getKey();
            field1.push(fieldName);
            BaseType value1 = field.getValue();

            if (hasField(node2, fieldName)) {
                // If both obj1 and obj2 have the same field, compare recursively
                field2.push(fieldName);
                BaseType value2 = getField(node2, fieldName);
                ret.add(compareNodes(field1, value1, field2, value2));
                field2.pop();
            } else {
                // obj1.field1 exists, obj2.field1 does not, so it is removed
                ret.add(new Removal(field1, value1));
            }
            field1.pop();
        }
        // Now compare any new nodes added to obj2
        for (Iterator<Map.Entry<String, BaseType>> fields = getFields(node2); fields.hasNext();) {
            Map.Entry<String, BaseType> field = fields.next();
            String fieldName = field.getKey();
            if (!hasField(node1, fieldName)) {
                field2.push(fieldName);
                ret.add(new Addition(field2, field.getValue()));
                field2.pop();
            }
        }
        return ret;
    }

    public IdentityExtractor getArrayIdentityExtractor(Path arrayField) {
        MutablePath p = new MutablePath();
        int n = arrayField.numSegments();
        for (int i = 0; i < n; i++) {
            if (arrayField.isIndex(i)) {
                p.push(Path.ANY);
            } else {
                p.push(arrayField.head(i));
            }
        }
        ArrayIdentityFields fields = arrayIdentities.get(p);
        if (fields != null) {
            return getArrayIdentityExtractorImpl(fields);
        } else {
            return null;
        }
    }

    public Difference<BaseType> compareArrays(MutablePath field1,
                                              ArrayType node1,
                                              MutablePath field2,
                                              ArrayType node2)
            throws InvalidArrayIdentity, DuplicateArrayIdentity {
        IdentityExtractor ext = getArrayIdentityExtractor(field1);
        if (ext == null) {
            return compareArraysNoId(field1, node1, field2, node2);
        } else {
            return compareArraysWithId(field1, node1, field2, node2, ext);
        }
    }

    /**
     * Computes difference between arrays whose elements can be identitied by a
     * unique identifier
     */
    public Difference<BaseType> compareArraysWithId(MutablePath field1,
                                                    ArrayType node1,
                                                    MutablePath field2,
                                                    ArrayType node2,
                                                    IdentityExtractor idex)
            throws InvalidArrayIdentity, DuplicateArrayIdentity {
        Difference<BaseType> ret = new Difference<>();
        // Build a map of identity -> index for both arrays
        final Map<Object, Integer> identities1 = getIdentityMap(field1, node1, idex);
        final Map<Object, Integer> identities2 = getIdentityMap(field2, node2, idex);

        // Iterate all elements of array 1
        for (Map.Entry<Object, Integer> entry1 : identities1.entrySet()) {
            // Append index to the field name
            field1.push(entry1.getValue());

            // If array2 doesn't have an element with the same ID, this is a deletion
            Integer index2 = identities2.get(entry1.getKey());
            if (index2 == null) {
                ret.add(new Removal(field1, getElement(node1, entry1.getValue())));
            } else {
                field2.push(index2);
                // array2 has the same element
                // If it is at a different index, this is a move
                if (index2 != entry1.getValue()) {
                    ret.add(new Move(field1, field2, getElement(node1, entry1.getValue())));
                }
                // Recursively compare contents to get detailed diff
                ret.add(compareNodes(field1, getElement(node1, entry1.getValue()),
                        field2, getElement(node2, index2)));
                field2.pop();
            }
            field1.pop();
        }
        // Now check elements of array 2 that are not in array 1
        for (Map.Entry<Object, Integer> entry2 : identities2.entrySet()) {
            if (!identities1.containsKey(entry2.getKey())) {
                // entry2 is not in array 1: addition
                field2.push(entry2.getValue());
                ret.add(new Addition(field2, getElement(node2, entry2.getValue())));
                field2.pop();
            }
        }
        return ret;
    }

    private static class Pair {
        private final int i1, i2;

        public int hashCode() {
            return i1 * 1001 + i2;
        }

        public boolean equals(Object o) {
            try {
                return ((Pair) o).i1 == i1
                        && ((Pair) o).i2 == i2;
            } catch (Exception e) {
                return false;
            }
        }

        public Pair(int i1, int i2) {
            this.i1 = i1;
            this.i2 = i2;
        }
    }

    /**
     * Computes difference between arrays by comparing every element recursively
     * and trying to find the closest match
     */
    public Difference<BaseType> compareArraysNoId(MutablePath field1,
                                                  ArrayType node1,
                                                  MutablePath field2,
                                                  ArrayType node2)
            throws InvalidArrayIdentity, DuplicateArrayIdentity {
        Difference<BaseType> ret = new Difference<>();
        IndexAssoc assoc = new IndexAssoc(size(node1), size(node2));
        HashSet<Pair> comparedPairs = new HashSet<>();

        // First associate exact matches
        // We loop through the unassociated elements of node1, and node2
        // If the nodes are equal, we associate them
        // if they are not, we note the distance between the two, so later
        // we don't need to re-compare them
        for (assoc.start1(); assoc.hasNext1();) {
            int index1 = assoc.next1();
            BaseType element1 = getElement(node1, index1);
            field1.push(index1);

            for (assoc.start2(); assoc.hasNext2();) {
                int index2 = assoc.next2();
                BaseType element2 = getElement(node2, index2);
                field2.push(index2);

                comparedPairs.add(new Pair(index1, index2));

                Difference diff = compareNodes(field1, element1, field2, element2);
                if (diff.same()) {
                    assoc.associate(index1, index2);
                    field2.pop();
                    break;
                } else {
                    assoc.recordDistance(index1, index2, diff);
                }
                field2.pop();
            }
            field1.pop();
        }

        // Here, we associated all exact matching nodes
        // All remaining nodes need to be compared to each other
        // First compare all node1 elements to node2 elements
        for (assoc.start1(); assoc.hasNext1();) {
            int index1 = assoc.next1();
            BaseType element1 = getElement(node1, index1);
            field1.push(index1);

            for (assoc.start2(); assoc.hasNext2();) {
                int index2 = assoc.next2();
                BaseType element2 = getElement(node2, index2);
                field2.push(index2);

                Pair p = new Pair(index1, index2);
                // Do we have a distance recorded for these nodes?
                if (comparedPairs.contains(p)) {
                    // No distance: compare the nodes
                    Difference diff = compareNodes(field1, element1, field2, element2);
                    assoc.recordDistance(index1, index2, diff);
                    comparedPairs.add(p);
                }
                field2.pop();
            }
            IxDiff ixdiff = assoc.getMin(index1);
            // If an object has changed more that 0.5 (more than half
            // of its fields are changed), then it is not a match
            if (ixdiff == null || ixdiff.change > 0.5) {
                // No matching node for node1
                ret.add(new Removal(field1, element1));
                assoc.remove1(index1);
            } else {
                // Matching node
                assoc.associate(index1, ixdiff.index2);
                ret.add(ixdiff.diff);
            }
            field1.pop();
        }
        // Anything remaining on node2 are nodes that are added
        for (assoc.start2(); assoc.hasNext2();) {
            int index = assoc.next2();
            BaseType element2 = getElement(node2, index);
            field2.push(index);
            ret.add(new Addition(field2, element2));
            field2.pop();
        }
        // Look at associations for moved nodes
        for (Map.Entry<Integer, Integer> entry : assoc.assoc.entrySet()) {
            if (entry.getKey() != entry.getValue()) {
                field1.push(entry.getKey());
                field2.push(entry.getValue());
                BaseType node = getElement(node1, entry.getKey());
                ret.add(new Move(field1, field2, node));
                field2.pop();
                field1.pop();
            }
        }
        return ret;
    }

    /**
     * Keeps the distance between two array indexes.
     */
    private static class IxDiff<T> {
        private Difference<T> diff;
        private double change;
        private int index1, index2;

        public IxDiff(Difference<T> diff,
                      double change,
                      int index1,
                      int index2) {
            this.diff = diff;
            this.change = change;
            this.index1 = index1;
            this.index2 = index2;
        }
    }

    /**
     * Keeps associations between array indexes
     */
    private static class IndexAssoc {
        private final ArrayList<Integer> ix1 = new ArrayList<>();
        private final ArrayList<Integer> ix2 = new ArrayList<>();
        private int itr1;
        private int itr2;
        private int last1, last2;
        private final Map<Integer, Integer> assoc = new HashMap<>();
        /**
         * Keeps the IxDiff with the minimum change between index1 and index2,
         * keyed on index1
         */
        private final Map<Integer, IxDiff> minimums1 = new HashMap<>();

        /**
         * Construct with two arrays of size1 and size2
         */
        public IndexAssoc(int size1, int size2) {
            for (int i = 0; i < size1; i++) {
                ix1.add(i);
            }
            for (int i = 0; i < size2; i++) {
                ix2.add(i);
            }
        }

        /**
         * Start iterating the unassociated indexes of the first array
         */
        public void start1() {
            itr1 = -1;
        }

        /**
         * Returns true if the first array has more unassociated indexes
         */
        public boolean hasNext1() {
            return (itr1 + 1) < ix1.size();
        }

        /**
         * Returns the current unassociated index of the first array, moves to
         * the next unassociated index
         */
        public int next1() {
            itr1++;
            return last1 = ix1.get(itr1);
        }

        public void remove1(int index) {
            int l1 = ix1.indexOf(index);
            if (l1 >= 0) {
                ix1.remove(l1);
                if (itr1 >= l1) {
                    itr1--;
                }
            }
        }

        /**
         * Start iterating the unassociated indexes of the second array
         */
        public void start2() {
            itr2 = -1;
        }

        /**
         * Returns true if the second array has more unassociated indexes
         */
        public boolean hasNext2() {
            return (itr2 + 1) < ix2.size();
        }

        /**
         * Returns the current unassociated index of the second array, moves to
         * the next unassociated index
         */
        public int next2() {
            itr2++;
            return last2 = ix2.get(itr2);
        }

        public void remove2(int index) {
            int l2 = ix2.indexOf(index);
            if (l2 >= 0) {
                ix2.remove(l2);
                if (itr2 >= l2) {
                    itr2--;
                }
            }

        }

        public void associate(int index1, int index2) {
            remove1(index1);
            remove2(index2);
            assoc.put(index1, index2);
        }

        /**
         * Records the amount of difference between the nodes last returned by
         * next(). Also stores the minumum amount of difference for node1
         */
        public void recordDistance(int index1, int index2, Difference diff) {
            double change = diff.getChangeAmount();
            IxDiff m = minimums1.get(index1);
            if (m == null || m.change > change) {
                minimums1.put(index1, m = new IxDiff(diff, change, index1, index2));
            }
        }

        public IxDiff getMin(int index1) {
            return minimums1.get(index1);
        }
    }

    private Map<Object, Integer> getIdentityMap(Path field, ArrayType array, IdentityExtractor idex)
            throws InvalidArrayIdentity, DuplicateArrayIdentity {
        final int size = size(array);
        final Map<Object, Integer> identities = new HashMap<>(size);
        // Fill up identities into identity maps
        for (int i = 0; i < size; i++) {
            Object id = idex.getIdentity(getElement(array, i));
            if (id == null) {
                throw new InvalidArrayIdentity(new Path(field,new Path(Integer.toString(i))));
            }
            if (identities.put(id, i) != null) {
                throw new DuplicateArrayIdentity(new Path(field,new Path(Integer.toString(i))));
            }
        }
        return identities;
    }
}
