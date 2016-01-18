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

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonCompareTest {

    public static JsonNode json(String s) throws Exception {
        return JsonUtils.json(s.replaceAll("\'","\""));
    }

    /**
     * Compare identical docs, no deltas
     */
    @Test
    public void testEq() throws Exception {
        JsonCompare cmp=new JsonCompare();
        JsonNode doc1=json("{'a':1,'b':'x','c':true,'d':[1,2,3],'e':[{'q':1,'w':'x'}]}");
        JsonNode doc2=json("{'b':'x','a':1,'c':true,'d':[1,2,3],'e':[{'q':1,'w':'x'}]}");
        JsonCompare.Difference diff=cmp.compareNodes(doc1,doc2);
        Assert.assertTrue(diff.same());
    }

    /**
     * Compare docs with one change to a field
     */
    @Test
    public void testSimpleDiff() throws Exception {
        JsonCompare cmp=new JsonCompare();
        JsonNode doc1=json("{'a':1,'b':'y','c':true,'d':[1,2,3],'e':[{'q':1,'w':'x'}]}");
        JsonNode doc2=json("{'b':'x','a':1,'c':true,'d':[1,2,3],'e':[{'q':1,'w':'x'}]}");
        JsonCompare.Difference diff=cmp.compareNodes(doc1,doc2);
        Assert.assertFalse(diff.same());
        Assert.assertEquals(1,diff.getNumChangedFields());
        Assert.assertEquals(1,diff.getDelta().size());
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Modification.class,"b",null));
    }

    /**
     * Compare docs with one primitive array element modification. Should show up as add/remove
     */
    @Test
    public void testSimpleArrayDiff() throws Exception {
        JsonCompare cmp=new JsonCompare();
        JsonNode doc1=json("{'a':1,'b':'y','c':true,'d':[1,2,3],'e':[{'q':1,'w':'x'}]}");
        JsonNode doc2=json("{'b':'y','a':1,'c':true,'d':[1,2,4],'e':[{'q':1,'w':'x'}]}");
        JsonCompare.Difference diff=cmp.compareNodes(doc1,doc2);
        Assert.assertFalse(diff.same());
        Assert.assertEquals(2,diff.getNumChangedFields());
        Assert.assertEquals(2,diff.getDelta().size());
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Removal.class,"d.2",null));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Addition.class,null,"d.2"));
    }

    /**
     * Compare docs with one primitive array element addition/removal.
     */
    @Test
    public void testSimpleArrayDiff_add() throws Exception {
        JsonCompare cmp=new JsonCompare();
        JsonNode doc1=json("{'a':1,'b':'y','c':true,'d':[1,2,3],'e':[{'q':1,'w':'x'}]}");
        JsonNode doc2=json("{'b':'y','a':1,'c':true,'d':[1,2,4,3],'e':[{'q':1,'w':'x'}]}");
        JsonCompare.Difference diff=cmp.compareNodes(doc1,doc2);
        Assert.assertFalse(diff.same());
        Assert.assertEquals(1,diff.getNumChangedFields());
        Assert.assertEquals(2,diff.getDelta().size());
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Addition.class,null,"d.2"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Move.class,"d.2","d.3"));
    }
    
    /**
     * Compare docs with one primitive array element addition/removal.
     */
    @Test
    public void testSimpleArrayDiff_addRemove() throws Exception {
        JsonCompare cmp=new JsonCompare();
        JsonNode doc1=json("{'a':1,'b':'y','c':true,'d':[1,2,3,4,5,6,7,8,9],'e':[{'q':1,'w':'x'}]}");
        JsonNode doc2=json("{'b':'y','a':1,'c':true,'d':[1,2,4,5,11,12,8,9,14],'e':[{'q':1,'w':'x'}]}");
        JsonCompare.Difference diff=cmp.compareNodes(doc1,doc2);
        Assert.assertFalse(diff.same());
        Assert.assertEquals(10,diff.getDelta().size());
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Removal.class,"d.2",null));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Removal.class,"d.5",null));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Removal.class,"d.6",null));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Addition.class,null,"d.4"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Addition.class,null,"d.5"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Addition.class,null,"d.8"));

        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Move.class,"d.3","d.2"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Move.class,"d.4","d.3"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Move.class,"d.7","d.6"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Move.class,"d.8","d.7"));
    }

    /**
     * Modifications to array of objects
     */
    @Test
    public void testObjectArrayDiff_addRemove() throws Exception {
        JsonCompare cmp=new JsonCompare();
        JsonNode doc1=json("{'a':1,'b':'y','c':true,'d':[ {'q':'1','w':'2'}, {'q':'3','w':'4'},{'q':'5','w':'6'}]}");
        JsonNode doc2=json("{'b':'y','a':1,'c':true,'d':[ {'q':'1','w':'22'}, {'q':'3','w':'4'},{'q':'7','w':'8'}]}");
        JsonCompare.Difference diff=cmp.compareNodes(doc1,doc2);
        Assert.assertFalse(diff.same());
        Assert.assertEquals(3,diff.getDelta().size());
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Removal.class,"d.2",null));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Addition.class,null,"d.2"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Modification.class,null,"d.0.w"));
   }

    /**
     * Modifications to array of objects
     */
    @Test
    public void testObjectArrayDiff_addObj() throws Exception {
        JsonCompare cmp=new JsonCompare();
        JsonNode doc1=json("{'d':[ {'q':'1','w':'2'}, {'q':'3','w':'4'},{'q':'5','w':'6'}]}");
        JsonNode doc2=json("{'d':[ {'q':'a'}, {'q':'1','w':'22'}, {'q':'3','w':'4'},{'q':'5','w':'6'}]}");
        JsonCompare.Difference diff=cmp.compareNodes(doc1,doc2);
        Assert.assertFalse(diff.same());
        Assert.assertEquals(5,diff.getDelta().size());
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Addition.class,null,"d.0"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Modification.class,null,"d.1.w"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Move.class,"d.0","d.1"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Move.class,"d.1","d.2"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Move.class,"d.2","d.3"));
   }

    /**
     * Modifications to array of objects w/id
     */
    @Test
    public void testObjectArrayDiff_addRemove_wid() throws Exception {
        JsonCompare cmp=new JsonCompare();
        cmp.addArrayIdentity(new Path("d"),new Path("id"));
        JsonNode doc1=json("{'a':1,'b':'y','c':true,'d':[ {'id':1,'q':'1','w':'2'}, {'id':2,'q':'3','w':'4'},{'id':3,'q':'5','w':'6'}]}");
        JsonNode doc2=json("{'b':'y','a':1,'c':true,'d':[ {'id':1,'q':'1','w':'22'}, {'id':2,'q':'3','w':'4'},{'id':3,'q':'7','w':'8'}]}");
        JsonCompare.Difference diff=cmp.compareNodes(doc1,doc2);
        System.out.println(diff);
        Assert.assertFalse(diff.same());
        Assert.assertEquals(3,diff.getDelta().size());
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Modification.class,null,"d.0.w"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Modification.class,null,"d.2.q"));
        Assert.assertTrue(hasDelta(diff.getDelta(),JsonCompare.Modification.class,null,"d.2.w"));
   }
    
    /**
     * Returns if the given type delta exists, with given fields. Any field can be null
     */
    private static boolean hasDelta(List<JsonCompare.Delta> delta,Class deltaType,String field1,String field2) {
        return hasDelta(delta,deltaType,field1==null?null:new Path(field1),field2==null?null:new Path(field2));
    }
    
    private static boolean hasDelta(List<JsonCompare.Delta> delta,Class deltaType,Path field1,Path field2) {
        boolean exists=false;
        for(JsonCompare.Delta d:delta) {
            if(d.getClass().isAssignableFrom(deltaType)) {
                boolean f1,f2;
                if(field1!=null)
                    f1=field1.equals(d.getField1());
                else
                    f1=true;
                if(field2!=null)
                    f2=field2.equals(d.getField2());
                else
                    f2=true;
                if(f1&&f2) {
                    exists=true;
                    break;
                }
            }
        }
        return exists;
    }
    
}
