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

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

import com.redhat.lightblue.metadata.types.DefaultTypes;

import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;

public class CompositeMetadataTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    public class TestDataStoreParser<T> implements DataStoreParser<T> {
        
        @Override
            public DataStore parse(String name, MetadataParser<T> p, T node) {
            return new DataStore() {
                public String getBackend() {
                    return "mongo";
                }
            };
        }
        
        @Override
            public void convert(MetadataParser<T> p, T emptyNode, DataStore object) {
        }
        
        @Override
            public String getDefaultName() {
            return "mongo";
        }
    }

    private EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        return parser.parseEntityMetadata(node);
    }

    private class SimpleGMD implements CompositeMetadata.GetMetadata {
        public EntityMetadata getMetadata(Path injectionField,
                                          String entityName,
                                          String version) {
            try {
                return getMd("composite/"+entityName+".json");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class LimitingGMD implements CompositeMetadata.GetMetadata {

        private final int limit;

        public LimitingGMD(int limit) {
            this.limit=limit;
        }

        public EntityMetadata getMetadata(Path injectionField,
                                          String entityName,
                                          String version) {
            try {
                if(injectionField.numSegments()>limit)
                    return null;
                return getMd("composite/"+entityName+".json");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void c_test() throws Exception {
        // C has two children, B and D
        EntityMetadata md=getMd("composite/C.json");
        CompositeMetadata c=CompositeMetadata.buildCompositeMetadata(md,new SimpleGMD());
        System.out.println(c.toTreeString());
        Assert.assertEquals("",c.getEntityPath().toString());
        Assert.assertEquals(2,c.getChildNames().size());
        CompositeMetadata b=c.getChildMetadata(new Path("b"));
        Assert.assertNotNull(b);
        CompositeMetadata d=c.getChildMetadata(new Path("obj1.d"));
        Assert.assertNotNull(d);
        Assert.assertNull(c.getParent());
        
        Assert.assertEquals(0,b.getChildNames().size());
        Assert.assertEquals(c,b.getParent());
        Assert.assertEquals("b",b.getEntityPath().toString());

        Assert.assertEquals(0,d.getChildNames().size());
        Assert.assertEquals(c,d.getParent());
        Assert.assertEquals("obj1.d",d.getEntityPath().toString());
    }

    @Test
    public void d_test() throws Exception {
        // D has no children
        EntityMetadata md=getMd("composite/D.json");
        CompositeMetadata d=CompositeMetadata.buildCompositeMetadata(md,new SimpleGMD());
        System.out.println(d.toTreeString());
        Assert.assertEquals("",d.getEntityPath().toString());
        Assert.assertEquals(0,d.getChildNames().size());
    }

    @Test
    public void a_test() throws Exception {
        // A has two children
        EntityMetadata md=getMd("composite/A.json");
        CompositeMetadata a=CompositeMetadata.buildCompositeMetadata(md,new SimpleGMD());

        System.out.println(a.toTreeString());
        Assert.assertEquals("",a.getEntityPath().toString());
        Assert.assertEquals(2,a.getChildNames().size());
        Assert.assertNull(a.getParent());
        CompositeMetadata b=a.getChildMetadata(new Path("b"));
        Assert.assertNotNull(b);
        CompositeMetadata c=a.getChildMetadata(new Path("obj1.c"));
        Assert.assertNotNull(c);
        
        Assert.assertEquals(0,b.getChildNames().size());
        Assert.assertEquals(a,b.getParent());
        Assert.assertEquals("b",b.getEntityPath().toString());

        Assert.assertEquals(2,c.getChildNames().size());
        CompositeMetadata cb=c.getChildMetadata(new Path("obj1.c.*.b"));
        Assert.assertNotNull(cb);
        CompositeMetadata cd=c.getChildMetadata(new Path("obj1.c.*.obj1.d"));
        Assert.assertNotNull(cd);
        Assert.assertEquals("obj1.c.*.obj1.d",cd.getEntityPath().toString());
        Assert.assertEquals(0,cd.getChildNames().size());

        Assert.assertEquals(a,c.getParent());
        Assert.assertEquals("obj1.c",c.getEntityPath().toString());

        Assert.assertEquals("obj1.c.*.b",cb.getEntityPath().toString());
        Assert.assertEquals(0,cb.getChildNames().size());
    }


    @Test
    public void r_test() throws Exception {
        // r has three children, and recursive
        EntityMetadata md=getMd("composite/R.json");
        CompositeMetadata r=CompositeMetadata.buildCompositeMetadata(md,new LimitingGMD(9));

        System.out.println(r.toTreeString());

        Assert.assertEquals("",r.getEntityPath().toString());
        Assert.assertEquals(3,r.getChildNames().size());
        Assert.assertNull(r.getParent());

        CompositeMetadata b=r.getChildMetadata(new Path("b"));
        Assert.assertNotNull(b);
        CompositeMetadata c=r.getChildMetadata(new Path("obj1.c"));
        Assert.assertNotNull(c);

        CompositeMetadata rr=r.getChildMetadata(new Path("r"));
        Assert.assertNotNull(rr);
    }

    @Test
    public void fields_a() throws Exception {
        EntityMetadata md=getMd("composite/A.json");
        CompositeMetadata a=CompositeMetadata.buildCompositeMetadata(md,new SimpleGMD());

        Assert.assertEquals(6,a.getEntitySchema().getFields().getNumChildren());
        ArrayField bfields=(ArrayField)a.getEntitySchema().getFields().getField("b");
        Assert.assertEquals(4,((ObjectArrayElement)bfields.getElement()).getFields().getNumChildren());
    }


    @Test
    public void resolvedRefTest() throws Exception {
        EntityMetadata md=getMd("composite/R.json");
        CompositeMetadata r=CompositeMetadata.buildCompositeMetadata(md,new LimitingGMD(9));

        ResolvedReferenceField ref=(ResolvedReferenceField)r.resolve(new Path("obj1.c"));
        Assert.assertEquals(r.getChildMetadata(new Path("obj1.c")),ref.getReferencedMetadata());
        
        ref=(ResolvedReferenceField)r.resolve(new Path("obj1.c.*.b"));
        Assert.assertEquals(r.getChildMetadata(new Path("obj1.c")).getChildMetadata(new Path("obj1.c.*.b")),ref.getReferencedMetadata());
    }

    @Test
    public void fieldResolutionTest() throws Exception {
        EntityMetadata md=getMd("composite/A.json");
        CompositeMetadata a=CompositeMetadata.buildCompositeMetadata(md,new SimpleGMD());

        ResolvedReferenceField ref=(ResolvedReferenceField)a.resolve(new Path("obj1.c"));
        Assert.assertNotNull(ref);

        SimpleField field=(SimpleField)ref.getElement().resolve(new Path("_id"));
        Assert.assertTrue(field==a.resolve(new Path("obj1.c.*._id")));
        Assert.assertEquals("obj1.c.*._id",field.getFullPath().toString());
    }
}
