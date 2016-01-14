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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Compares two json documents and builds an list of all changes
 *
 * Json containers (objects and arrays) are compared recursively. The
 * comparison algorithm works like this:
 *
 * Objects: A field-by-field comparison is done. If a field exists in
 * the first document but not in the second, that field is removed. If
 * a field exists in the second document but not the first, that field
 * is added. If a field exists in both documents with different
 * values, that field is modified.
 *
 * Arrays: There are two possible algorithms to compare arrays. If
 * array elements contain a unique identifier (which is defined by the
 * caller), then array elelements of the first and the second document
 * are matched using the unique identifiers of array elements. Then
 * each matching array element is compared to generate the detailed
 * difference. If array elements don't have unique identifiers, then
 * each element of the first array is compared to each element of the
 * second array, and the elements with minimal number of changes are
 * associated. Elements that are too different from each other are not
 * associated.
 */
public class JsonCompare {

    /**
     * Thrown if there is an array whose elements contain identities,
     * but for at least one element identity cannot be retrieved
     */
    public static final class InvalidArrayIdentity extends Exception {
        public InvalidArrayIdentity(Path p) {
            super(p.toString());
        }
    }

    /**
     * Thrown if there is an array whose elements contain identities,
     * but they are not unique
     */
    public static final class DuplicateArrayIdentity extends Exception {
        public DuplicateArrayIdentity(Path p) {
            super(p.toString());
        }
    }

    /**
     * The array element identity extractor interface. 
     */
    public interface IdentityExtractor {

        /**
         * It should return an identity object from the given array
         * element. The returned object should implement equals and
         * hashCode methods.
         */
        Object getIdentity(JsonNode element);
    }


    /**
     * Contains the edit script, and number of changed and unchanged
     * fields. The number of changed fields does not include array
     * element moves, it only includes additions, removals, and
     * modifications.
     */
    public static class Difference {
        private final List<Delta> delta;
        private int numUnchangedFields;
        private int numChangedFields;

        /**
         * Default ctor, sets numUnchangedFields to zero, initializes an empty list
         */
        public Difference() {
            delta=new ArrayList();
            numUnchangedFields=0;
        }
        
        public Difference(List<Delta> delta) {
            this.delta=delta;
            for(Delta d:this.delta) {
                if(d instanceof Addition||
                   d instanceof Removal ||
                   d instanceof Modification)
                    numChangedFields++;
            }
        }

        /**
         * Constructs a difference with one modification, no unchanged fields
         */
        public Difference(Delta d) {
            this(new ArrayList<Delta>(1));
            add(d);
            
        }

        /**
         * Constructs a Difference denoting no difference
         */
        public Difference(int numFields) {
            delta=new ArrayList<Delta>();
            numUnchangedFields=numFields;
        }

        public int getNumUnchangedFields() {
            return numUnchangedFields;
        }

        public int getNumChangedFields() {
            return numChangedFields;
        }

        public List<Delta> getDelta() {
            return delta;
        }

        public void add(Difference diff) {
            delta.addAll(diff.delta);
            numUnchangedFields+=diff.numUnchangedFields;
            numChangedFields+=diff.numChangedFields;
        }

        public void add(Delta d) {
            delta.add(d);
            if(d instanceof Addition||
               d instanceof Removal ||
               d instanceof Modification)
                numChangedFields++;
        }

        public void unchanged() {
            numUnchangedFields++;
        }

        public double getChangeAmount() {
            double d=numChangedFields+numUnchangedFields;
            return d==0?0:numChangedFields/d;
        }

        public boolean same() {
            return delta.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder bld=new StringBuilder();
            for(Delta x:delta) {
                bld.append(x.toString()).append('\n');
            }
            return bld.toString();
        }
    }
    
    public static abstract class Delta {
        protected final Path field1;
        protected final Path field2;

        public Delta(Path field1,Path field2) {
            this.field1=field1==null?null:field1.immutableCopy();
            this.field2=field2==null?null:field2.immutableCopy();
        }

        public Path getField1() {
            return field1;
        }

        public Path getField2() {
            return field2;
        }
    }

    public static class Addition extends Delta {
        private final JsonNode addedNode;

        public Addition(Path field2,JsonNode addedNode) {
            super(null,field2);
            this.addedNode=addedNode;
        }

        @Override
        public String toString() {
            return "+ "+field2+":"+addedNode;
        }
    }

    public static class Removal extends Delta {
        private final JsonNode removedNode;

        public Removal(Path field1,JsonNode removedNode) {
            super(field1,null);
            this.removedNode=removedNode;
        }

        @Override
        public String toString() {
            return "- "+field1+":"+removedNode;
        }
    }

    public static class Move extends Delta {
        private final JsonNode movedNode;
        
        public Move(Path field1,Path field2,JsonNode movedNode) {
            super(field1,field2);
            this.movedNode=movedNode;
        }

        @Override
        public String toString() {
            return "* "+field1+"->"+field2+":"+movedNode;
        }
    }

    public static class Modification extends Delta {
        private final JsonNode node1;
        private final JsonNode node2;

        public Modification(Path field1,JsonNode node1,Path field2,JsonNode node2) {
            super(field1,field2);
            this.node1=node1;
            this.node2=node2;
        }

        @Override
        public String toString() {
            return "* "+field1+"->"+field2+":"+node1+" -> "+node2;
        }
   }


    public static class ArrayIdentityFields {
        private Path[] fields;

        public ArrayIdentityFields(Path...fields) {
            this.fields=fields;
        }

        public Path[] getFields() {
            return fields;
        }
    }

    public static class DefaultIdentity {
        private final JsonNode[] nodes;
        private Integer hcode;

        public DefaultIdentity(JsonNode[] nodes) {
            this.nodes=nodes;
        }

        public int hashCode() {
            if(hcode==null) {
                int code=0;
                for(int i=0;i<nodes.length;i++) {
                    if(nodes[i]!=null)
                        code+=nodes[i].hashCode();
                }
                hcode=code;
            }
            return hcode;
        }

        public boolean equals(Object x) {
            try {
                DefaultIdentity d=(DefaultIdentity)x;
                for(int i=0;i<nodes.length;i++) {
                    if(!d.nodes[i].equals(nodes[i]))
                        return false;
                }
            } catch (Exception e) {
                return false;
            }   

            return true;
        }
    }

    public static class DefaultIdentityExtractor implements IdentityExtractor {
        private final Path[] fields;

        public DefaultIdentityExtractor(ArrayIdentityFields fields) {
            this.fields=fields.fields;
        }

        @Override
        public Object getIdentity(JsonNode element) {
            JsonNode[] nodes=new JsonNode[fields.length];
            for(int i=0;i<fields.length;i++) {
                nodes[i]=JsonDoc.get(element,fields[i]);
            }
            return new DefaultIdentity(nodes);
        }
    }

    private final Map<Path,ArrayIdentityFields> arrayIdentities=new HashMap<>();

    public void addArrayIdentity(Path array,Path...identities) {
        arrayIdentities.put(array,new ArrayIdentityFields(identities));
    }

    public Difference compareNodes(JsonNode node1,JsonNode node2) 
         throws InvalidArrayIdentity, DuplicateArrayIdentity {
       return compareNodes(new MutablePath(),node1,new MutablePath(),node2);
    }
    
    public Difference compareNodes(MutablePath field1,
                                   JsonNode node1,
                                   MutablePath field2,
                                   JsonNode node2) 
        throws InvalidArrayIdentity, DuplicateArrayIdentity {
        if(node1 instanceof ValueNode && node2 instanceof ValueNode) {
            if(!equals((ValueNode)node1,(ValueNode)node2)) {
                return new Difference(new Modification(field1,node1,field2,node2));
            }
        } else if(node1 instanceof ArrayNode && node2 instanceof ArrayNode) {
            return compareArrays(field1,(ArrayNode)node1,field2,(ArrayNode)node2);
        } else if(node1 instanceof ObjectNode && node2 instanceof ObjectNode) {
            return compareObjects(field1,(ObjectNode)node1,field2,(ObjectNode)node2);
        } else {
            if(!(node1 instanceof NullNode && node2 instanceof NullNode) ) {
                return new Difference(new Modification(field1,node1,field2,node2));
            }
        }
        return new Difference(1);
    }
    
    /**
     * Compares two object nodes recursively and returns the differences
     */
    public Difference compareObjects(MutablePath field1,
                                     ObjectNode node1,
                                     MutablePath field2,
                                     ObjectNode node2) 
        throws InvalidArrayIdentity, DuplicateArrayIdentity {
        Difference ret=new Difference();
        // Field by field comparison of obj1 to obj2. 
        for(Iterator<Map.Entry<String,JsonNode>> fields=node1.fields();fields.hasNext();) {
            Map.Entry<String,JsonNode> field=fields.next();
            String fieldName=field.getKey();
            field1.push(fieldName);
            JsonNode value1=field.getValue();
            
            if(node2.has(fieldName)) {
                // If both obj1 and obj2 have the same field, compare recursively
                field2.push(fieldName);
                JsonNode value2=node2.get(fieldName);
                ret.add(compareNodes(field1,value1,field2,value2));
                field2.pop();
            } else {
                // obj1.field1 exists, obj2.field1 does not, so it is removed
                ret.add(new Removal(field1,value1));
            }
            field1.pop();
        }
        // Now compare any new nodes added to obj2
        for(Iterator<Map.Entry<String,JsonNode>> fields=node2.fields();fields.hasNext();) {
            Map.Entry<String,JsonNode> field=fields.next();
            String fieldName=field.getKey();
            if(!node1.has(fieldName)) {
                field2.push(fieldName);
                ret.add(new Addition(field2,field.getValue()));
                field2.pop();
            }
        }
        return ret;
    }
        
    public boolean equals(ValueNode value1,ValueNode value2) {        
        if(value1.isNumber()&&value2.isNumber()) {
            return value1.asText().equals(value2.asText());
        } else {
            return value1.equals(value2);
        }
    }

    public IdentityExtractor getArrayIdentityExtractor(Path arrayField) {
        MutablePath p=new MutablePath();
        int n=arrayField.numSegments();
        for(int i=0;i<n;i++) {
            if(arrayField.isIndex(i))
                p.push(Path.ANY);
            else
                p.push(arrayField.head(i));
        }
        ArrayIdentityFields fields=arrayIdentities.get(p);
        if(fields!=null) {
            return new DefaultIdentityExtractor(fields);
        } else {
            return null;
        }
    }
    
    public Difference compareArrays(MutablePath field1,
                                    ArrayNode node1,
                                    MutablePath field2,
                                    ArrayNode node2) 
        throws InvalidArrayIdentity, DuplicateArrayIdentity {
        IdentityExtractor ext=getArrayIdentityExtractor(field1);
        if(ext==null) {
            return compareArraysNoId(field1,node1,field2,node2);
        } else {
            return compareArraysWithId(field1,node1,field2,node2,ext);
        }
    }

    /**
     * Computes difference between arrays whose elements can be identitied by a unique identifier
     */
    public Difference compareArraysWithId(MutablePath field1,
                                          ArrayNode node1,
                                          MutablePath field2,
                                          ArrayNode node2,
                                          IdentityExtractor idex)
        throws InvalidArrayIdentity, DuplicateArrayIdentity {
        Difference ret=new Difference();
        // Build a map of identity -> index for both arrays
        final Map<Object,Integer> identities1=getIdentityMap(field1,node1,idex);
        final Map<Object,Integer> identities2=getIdentityMap(field2,node2,idex);

        // Iterate all elements of array 1
        for(Map.Entry<Object,Integer> entry1:identities1.entrySet()) {
            // Append index to the field name
            field1.push(entry1.getValue());
            
            // If array2 doesn't have an element with the same ID, this is a deletion
            Integer index2=identities2.get(entry1.getKey());
            if(index2==null) {
                ret.add(new Removal(field1,node1.get(entry1.getValue())));
            } else {
                field2.push(index2);
                // array2 has the same element
                // If it is at a different index, this is a move
                if(index2!=entry1.getValue()) {
                    ret.add(new Move(field1,field2,node1.get(entry1.getValue())));
                }
                // Recursively compare contents to get detailed diff
                ret.add(compareNodes(field1,node1.get(entry1.getValue()),
                                     field2,node2.get(index2)));
                field2.pop();
            }
            field1.pop();
        }
        // Now check elements of array 2 that are not in array 1
        for(Map.Entry<Object,Integer> entry2:identities2.entrySet()) {
            if(!identities1.containsKey(entry2.getKey())) {
                // entry2 is not in array 1: addition
                field2.push(entry2.getValue());
                ret.add(new Addition(field2,node2.get(entry2.getValue())));
                field2.pop();
            }
        }
        return ret;
    }

    /**
     * Computes difference between arrays by comparing every element
     * recursively and trying to find the closest match
     */
    public Difference compareArraysNoId(MutablePath field1,
                                        ArrayNode node1,
                                        MutablePath field2,
                                        ArrayNode node2) 
        throws InvalidArrayIdentity, DuplicateArrayIdentity {
        Difference ret=new Difference();
        IndexAssoc assoc=new IndexAssoc(node1.size(),node2.size());

        // First associate exact matches
        // We loop through the unassociated elements of node1, and node2
        // If the nodes are equal, we associate them
        // if they are not, we note the distance between the two, so later
        // we don't need to re-compare them
        for(assoc.start1();assoc.hasNext1();) {
            int index=assoc.next1();
            JsonNode element1=node1.get(index);
            field1.push(index);

            for(assoc.start2();assoc.hasNext2();) {
                index=assoc.next2();
                JsonNode element2=node2.get(index);
                field2.push(index);

                Difference diff=compareNodes(field1,element1,field2,element2);
                if(diff.same()) {
                    assoc.associate();
                    field2.pop();
                    break;
                } else {
                    assoc.recordDistance(diff);
                }
                field2.pop();
            }
            field1.pop();
        }

        // Here, we associated all exact matching nodes
        // All remaining nodes need to be compared to each other
        // First compare all node1 elements to node2 elements
        for(assoc.start1();assoc.hasNext1();) {
            int index=assoc.next1();
            JsonNode element1=node1.get(index);
            field1.push(index);

            for(assoc.start2();assoc.hasNext2();) {
                int index2=assoc.next2();
                JsonNode element2=node2.get(index2);
                field2.push(index2);

                // Do we have a distance recorded for these nodes?
                if(assoc.getMinDiffFor1()==null) {
                    // No distance: compare the nodes
                    Difference diff=compareNodes(field1,element1,field2,element2);
                    assoc.recordDistance(diff);
                }
                field2.pop();
            }
            int index2=assoc.getMinChangeFor1();
            if(index2==-1) {
                // No matching node for node1
                ret.add(new Removal(field1,element1));
                assoc.remove1();
            } else {
                // Matching node
                Difference diff=assoc.getMinDiffFor1();
                assoc.associate(index,index2);
                ret.add(diff);
            }
            field1.pop();
        }
        // Anything remaining on node2 are nodes that are added
        for(assoc.start2();assoc.hasNext2();) {
            int index=assoc.next2();
            JsonNode element2=node2.get(index);
            field2.push(index);
            ret.add(new Addition(field2,element2));
            field2.pop();
        }
        // Look at associations for moved nodes
        for(Map.Entry<Integer,Integer> entry:assoc.assoc.entrySet()) {
            if(entry.getKey()!=entry.getValue()) {
                field1.push(entry.getKey());
                field2.push(entry.getValue());
                JsonNode node=node1.get(entry.getKey());
                ret.add(new Move(field1,field2,node));
                field2.pop();
                field1.pop();
            }
        }
        return ret;
    }

    private static class MinDiff {
        private Difference diff;
        private double change;
        private int minIndex;

        public MinDiff(Difference diff,
                       double change,
                       int minIndex) {
            this.diff=diff;
            this.change=change;
            this.minIndex=minIndex;
        }
    }

    /**
     * Keeps associations between array indexes
     */
    private static class IndexAssoc {
        private final ArrayList<Integer> ix1=new ArrayList<>();
        private final ArrayList<Integer> ix2=new ArrayList<>();
        private int itr1;
        private int itr2;
        private int last1,last2;
        private final Map<Integer,Integer> assoc=new HashMap<>();
        private final Map<Integer,MinDiff> minimums1=new HashMap<>();

        /**
         * Construct with two arrays of size1 and size2
         */
        public IndexAssoc(int size1,int size2) {
            for(int i=0;i<size1;i++)
                ix1.add(i);
            for(int i=0;i<size2;i++)
                ix2.add(i);
        }

        /**
         * Start iterating the unassociated indexes of the first array
         */
        public void start1() {
            itr1=-1;
        }

        /**
         * Returns true if the first array has more unassociated indexes
         */
        public boolean hasNext1() {
            return (itr1+1)<ix1.size();
        }

        /**
         * Returns the current unassociated index of the first array, moves to the next unassociated index
         */
        public int next1() {
            itr1++;
            return last1=ix1.get(itr1);
        }

        public void remove1() {
            ix1.remove(itr1);
            itr1--;
        }

        /**
         * Start iterating the unassociated indexes of the second array
         */
        public void start2() {
            itr2=-1;
        }

        /**
         * Returns true if the second array has more unassociated indexes
         */
        public boolean hasNext2() {
            return (itr2+1)<ix2.size();
        }

        /**
         * Returns the current unassociated index of the second array, moves to the next unassociated index
         */
        public int next2() {
            itr2++;
            return last2=ix2.get(itr2);
        }

        public void remove2() {
            ix2.remove(itr2);
            itr2--;
        }

        /**
         * Associates the indexes last returned by next(). These
         * indexes are removed from the unassociated list
         */
        public void associate() {
            remove1();
            remove2();
            assoc.put(last1,last2);
        }

        public void associate(int index1,int index2) {
        	int l1=ix1.indexOf(index1);
        	if(l1>=0) {
        		ix1.remove(l1);
        		if(l1>=itr1)
        			itr1--;
        	}
        	int l2=ix2.indexOf(index2);
        	if(l2>=0) {
        		ix2.remove(l2);
        		if(l2>=itr2)
        			itr2--;
        	}
            assoc.put(index1,index2);
        }

        /**
         * Records the amount of difference between the nodes last
         * returned by next(). Also stores the minumum amount of
         * difference for node1
         */
        public void recordDistance(Difference diff) {
            double change=diff.getChangeAmount();
            if(change<=0.5) { // Heuristic threshold: If more that 50% is changed, no match
                MinDiff m=minimums1.get(last1);
                if(m==null||m.change>change) {
                    minimums1.put(last1,m=new MinDiff(diff,change,last1));
                }
            }
        }

        
        public int getMinChangeFor1() {
            MinDiff d=minimums1.get(last1);
            return d==null?-1:d.minIndex;
        }

        public Difference getMinDiffFor1() {
            MinDiff d=minimums1.get(last1);
            return d==null?null:d.diff;
        }
    }
    
    private static Map<Object,Integer> getIdentityMap(Path field,ArrayNode array,IdentityExtractor idex)
        throws InvalidArrayIdentity, DuplicateArrayIdentity {
        final int size=array.size();
        final Map<Object,Integer> identities=new HashMap<>(size);
        // Fill up identities into identity maps
        for(int i=0;i<size;i++) {
            Object id=idex.getIdentity(array.get(i));
            if(id==null)
                throw new InvalidArrayIdentity(new Path(field,i));
            if(identities.put(id,i)!=null)
                throw new DuplicateArrayIdentity(new Path(field,i));
        }
        return identities;
    }
}
