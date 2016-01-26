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
package com.redhat.lightblue.eval;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonCompare;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class FieldAccessRoleEvaluatorTest extends AbstractJsonNodeTest {

    @Test
    public void testDateComparison() throws Exception {
        EntityMetadata md = EvalTestContext.getMd("./testMetadata.json");
        Set<String> roles=new HashSet<>();
        roles.add("somerole");
        FieldAccessRoleEvaluator eval=new FieldAccessRoleEvaluator(md,roles);
        JsonDoc newDoc = EvalTestContext.getDoc("./dateCmp-1.json");
        JsonDoc oldDoc = EvalTestContext.getDoc("./dateCmp-2.json");
        Set<Path> list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        Assert.assertEquals(0,list.size());
    }

    @Test
    public void testDiff_field1() throws Exception {
        EntityMetadata md = EvalTestContext.getMd("./testMetadata-restricted.json");
        JsonDoc oldDoc=EvalTestContext.getDoc("./doc-restricted.json");
        JsonDoc newDoc=EvalTestContext.getDoc("./doc-restricted.json");
        newDoc.modify(new Path("field1"),JSON_NODE_FACTORY.textNode("test"),true);
        Set<String> roles=new HashSet<>();
        roles.add("somerole");      
        FieldAccessRoleEvaluator eval=new FieldAccessRoleEvaluator(md,roles);

        Set<Path> list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        Assert.assertEquals(1,list.size());
        Assert.assertTrue(list.contains(new Path("field1")));
    }

    @Test
    public void testDiff_field6_nf1() throws Exception {
        EntityMetadata md = EvalTestContext.getMd("./testMetadata-restricted.json");
        JsonDoc oldDoc=EvalTestContext.getDoc("./doc-restricted.json");
        JsonDoc newDoc=EvalTestContext.getDoc("./doc-restricted.json");
        newDoc.modify(new Path("field6.nf1"),JSON_NODE_FACTORY.textNode("test"),true);
        Set<String> roles=new HashSet<>();
        roles.add("somerole");      
        FieldAccessRoleEvaluator eval=new FieldAccessRoleEvaluator(md,roles);

        Set<Path> list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        Assert.assertEquals(1,list.size());
        Assert.assertTrue(list.contains(new Path("field6.nf1")));
    }

    @Test
    public void testDiff_field6_nf5() throws Exception {
        EntityMetadata md = EvalTestContext.getMd("./testMetadata-restricted.json");
        JsonDoc oldDoc=EvalTestContext.getDoc("./doc-restricted.json");
        JsonDoc newDoc=EvalTestContext.getDoc("./doc-restricted.json");
        newDoc.modify(new Path("field6.nf5.5"),JSON_NODE_FACTORY.numberNode(1),true);
        Set<String> roles=new HashSet<>();
        roles.add("somerole");      
        FieldAccessRoleEvaluator eval=new FieldAccessRoleEvaluator(md,roles);

        Set<Path> list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        Assert.assertEquals(1,list.size());
        Assert.assertTrue(list.contains(new Path("field6.nf5")));
    }

    @Test
    public void testDiff_noidarr_field7() throws Exception {
        EntityMetadata md = EvalTestContext.getMd("./testMetadata-restricted.json");
        JsonDoc oldDoc=EvalTestContext.getDoc("./doc-restricted.json");
        JsonDoc newDoc=EvalTestContext.getDoc("./doc-restricted.json");
        Set<String> roles=new HashSet<>();
        roles.add("somerole");      
        FieldAccessRoleEvaluator eval=new FieldAccessRoleEvaluator(md,roles);
        
        newDoc.modify(new Path("field7.0.elemf1"),JSON_NODE_FACTORY.textNode("mod"),true);
        Set<Path> list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        Assert.assertEquals(1,list.size());
        Assert.assertTrue(list.contains(new Path("field7.0.elemf1")));

        // Insert new array element
        ObjectNode obj=JSON_NODE_FACTORY.objectNode();
        ((ArrayNode)newDoc.get(new Path("field7"))).insert(0,obj);
        obj.set("elemf1",JSON_NODE_FACTORY.textNode("text"));        
        list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        Assert.assertEquals(1,list.size());
        // Still field7.0 is changed, because 0 moved to 1, and that element is changed.
        Assert.assertTrue(list.contains(new Path("field7.0.elemf1")));
       
        // Remove array element
        newDoc.modify(new Path("field7.1"),null,true);
        list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        // Array element addition doesn't show up
        Assert.assertEquals(0,list.size());                      
    }

    @Test
    public void testDiff_idarr_field12() throws Exception {
        EntityMetadata md = EvalTestContext.getMd("./testMetadata-restricted.json");
        JsonDoc oldDoc=EvalTestContext.getDoc("./doc-restricted.json");
        JsonDoc newDoc=EvalTestContext.getDoc("./doc-restricted.json");
        Set<String> roles=new HashSet<>();
        roles.add("somerole");      
        FieldAccessRoleEvaluator eval=new FieldAccessRoleEvaluator(md,roles);

        // Swap two nodes--no diffs should show up
        ArrayNode arr=(ArrayNode)newDoc.get(new Path("field12.nf1.nnf1.0.nnnf1.arr"));
        JsonNode node=arr.get(0);
        arr.remove(0);
        arr.insert(1,node);
        Set<Path> list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        // Moving array elements doesn't violate any access
        Assert.assertEquals(0,list.size());
        JsonCompare.Difference diff=eval.getLastDiff();
        // There should be two move diffs, one for 0->1, and one for 1->0
        Assert.assertEquals(2,diff.getDelta().size());
        Assert.assertTrue(diff.getDelta().get(0) instanceof JsonCompare.Move);

        // Modify all fields, element identity should still associate the correct elements
        newDoc.modify(new Path("field12.nf1.nnf1.0.nnnf1.arr.0.narr"),null,true);
        newDoc.modify(new Path("field12.nf1.nnf1.0.nnnf1.arr.0.x1"),JSON_NODE_FACTORY.textNode("t"),true);
        list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        // Only setting x1 is invalid
        Assert.assertEquals(1,list.size());
        Assert.assertTrue(list.contains(new Path("field12.nf1.nnf1.0.nnnf1.arr.1.x1")));
    }    
}
