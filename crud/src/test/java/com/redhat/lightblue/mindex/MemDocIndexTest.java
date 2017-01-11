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
package com.redhat.lightblue.mindex;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;

import com.redhat.lightblue.assoc.QueryFieldInfo;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

import com.redhat.lightblue.TestDataStoreParser;

public class MemDocIndexTest extends AbstractJsonSchemaTest {

    private EntityMetadata getMd(String fname) {
        try {
            JsonNode node = loadJsonNode(fname);
            Extensions<JsonNode> extensions = new Extensions<>();
            extensions.addDefaultExtensions();
            extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
            TypeResolver resolver = new DefaultTypes();
            JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, JsonNodeFactory.instance);
            EntityMetadata md = parser.parseEntityMetadata(node);
            PredefinedFields.ensurePredefinedFields(md);
            return md;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replaceAll("\'", "\"")));
    }

    /**
     * field1: "field1:i"
     * field2: "field2:i"
     * field3: null
     " field4: null for odd docs, "1" for even docs
     * field7.i.elemf1: "doc:i elemf1:j"
     * field7.i.elemf2: "doc:i elemf2:j" fot even i, null for others
     */
    private List<JsonDoc> fill() {
        // Build doc list
        List<JsonDoc> docs=new ArrayList<>();
        for(int i=0;i<100;i++) {
            JsonDoc doc=new JsonDoc(JsonNodeFactory.instance.objectNode());
            doc.modify(new Path("objectType"),JsonNodeFactory.instance.textNode("test"),true);
            doc.modify(new Path("field1"),JsonNodeFactory.instance.textNode("field1:"+i),true);
            doc.modify(new Path("field2"),JsonNodeFactory.instance.textNode("field2:"+i),true);
            doc.modify(new Path("field2"),JsonNodeFactory.instance.textNode("field2:"+i),true);
            if(i%2==0) {
                doc.modify(new Path("field4"),JsonNodeFactory.instance.numberNode(i),true);
            }
            for(int j=0;j<100;j++) {
                doc.modify(new Path("field7."+j+".elemf1"),JsonNodeFactory.instance.textNode("doc:"+i+" elemf1:"+j),true);
                if(i%2==0) {
                    doc.modify(new Path("field7."+j+".elemf2"),JsonNodeFactory.instance.textNode("doc:"+i+" elemf2:"+j),true);
                }
            }
            docs.add(doc);
        }
        return docs;
    }

    public QueryFieldInfo qfi(EntityMetadata md,
                              String entityRelativeFieldName,
                              String entityRelativeFieldNameWithContext) {
        return new QueryFieldInfo(null,
                                  null,
                                  md.resolve(new Path(entityRelativeFieldNameWithContext)),
                                  null,
                                  new Path(entityRelativeFieldName),
                                  new Path(entityRelativeFieldNameWithContext),
                                  null,
                                  true);
    }

    @Test
    public void simpleValueLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // Simple indexing using field1
        SimpleKeySpec spec=new SimpleKeySpec(qfi(md,"field1","field1"));
        MemDocIndex index=new MemDocIndex(spec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);
        
        // Search
        Set<JsonDoc> results=index.find(new ValueLookupSpec(spec,"field1:10"));
        Assert.assertEquals(1,results.size());
        Assert.assertEquals("field1:10",results.iterator().next().get(new Path("field1")).asText());

        results=index.find(new ValueLookupSpec(spec,"field1:1011"));
        Assert.assertEquals(0,results.size());
    }

    @Test
    public void simpleValueNullLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // Simple indexing using field4 - it has nulls
        SimpleKeySpec spec=new SimpleKeySpec(qfi(md,"field4","field4"));
        MemDocIndex index=new MemDocIndex(spec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);
        
        // Search
        Set<JsonDoc> results=index.find(new ValueLookupSpec(spec,new Integer(10)));
        Assert.assertEquals(1,results.size());
        Assert.assertEquals("field1:10",results.iterator().next().get(new Path("field1")).asText());

        results=index.find(new ValueLookupSpec(spec,new Integer(11)));
        Assert.assertEquals(0,results.size());

        results=index.find(new ValueLookupSpec(spec,null));
        Assert.assertEquals(50,results.size());
    }

    @Test
    public void multiValueLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // Simple indexing using field1
        SimpleKeySpec spec=new SimpleKeySpec(qfi(md,"field1","field1"));
        MemDocIndex index=new MemDocIndex(spec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);
        
        // Search
        HashSet<Object> values=new HashSet<>();
        values.add("field1:10");
        values.add("field1:11");
        values.add("field1:1101");
        Set<JsonDoc> results=index.find(new MultiValueLookupSpec(spec,values));
        Assert.assertEquals(2,results.size());
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:10"));
    }

    @Test
    public void rangeValueLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // Simple indexeing using field1
        SimpleKeySpec spec=new SimpleKeySpec(qfi(md,"field1","field1"));
        MemDocIndex index=new MemDocIndex(spec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);

        Set<JsonDoc> results=index.find(new RangeLookupSpec(spec,"field1:10","field1:15"));
        Assert.assertEquals(6,results.size());
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:10"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:11"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:12"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:13"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:14"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:15"));
    }

    @Test
    public void prefixLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // Simple indexeing using field1
        SimpleKeySpec spec=new SimpleKeySpec(qfi(md,"field1","field1"));
        MemDocIndex index=new MemDocIndex(spec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);

        Set<JsonDoc> results=index.find(new PrefixLookupSpec(spec,"field1:1",true));
        Assert.assertEquals(11,results.size());
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:1"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:10"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:11"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:12"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:13"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:14"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:15"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:16"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:17"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:18"));
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:19"));
    }
    
    @Test
    public void twoSimpleValuesLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // indexeing using field1 and field2
        SimpleKeySpec spec1=new SimpleKeySpec(qfi(md,"field1","field1"));
        SimpleKeySpec spec2=new SimpleKeySpec(qfi(md,"field2","field2"));
        CompositeKeySpec aspec=new CompositeKeySpec(new KeySpec[] {spec1,spec2});
        MemDocIndex index=new MemDocIndex(aspec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);
        
        // Search
        Set<JsonDoc> results=index.find(new CompositeLookupSpec(new LookupSpec[] {new ValueLookupSpec(spec1,"field1:10"),
                                                                                  new ValueLookupSpec(spec2,"field2:10")}));
                                                                
        Assert.assertEquals(1,results.size());
        Assert.assertEquals("field1:10",results.iterator().next().get(new Path("field1")).asText());

        results=index.find(new CompositeLookupSpec(new LookupSpec[] {new ValueLookupSpec(spec1,"field1:10"),
                                                                     new ValueLookupSpec(spec2,"field2:11")}));
        Assert.assertEquals(0,results.size());
    }

    @Test
    public void twoMultiValueLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // indexing using field1 and field2
        SimpleKeySpec spec1=new SimpleKeySpec(qfi(md,"field1","field1"));
        SimpleKeySpec spec2=new SimpleKeySpec(qfi(md,"field2","field2"));
        CompositeKeySpec aspec=new CompositeKeySpec(new KeySpec[] {spec1,spec2});
        MemDocIndex index=new MemDocIndex(aspec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);
        
        // Search
        HashSet<Object> values1=new HashSet<>();
        values1.add("field1:10");
        values1.add("field1:11");
        values1.add("field1:1101");
        Set<JsonDoc> results=index.find(new CompositeLookupSpec(new LookupSpec[] {new MultiValueLookupSpec(spec1,values1),
                                                                                  new ValueLookupSpec(spec2,"field2:10")}));
        Assert.assertEquals(1,results.size());
        Assert.assertEquals("field1:10",results.iterator().next().get(new Path("field1")).asText());
    }

    @Test
    public void twoRangeValueLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // indexeing using field1 and field2
        SimpleKeySpec spec1=new SimpleKeySpec(qfi(md,"field1","field1"));
        SimpleKeySpec spec2=new SimpleKeySpec(qfi(md,"field2","field2"));
        CompositeKeySpec aspec=new CompositeKeySpec(new KeySpec[] {spec1,spec2});
        MemDocIndex index=new MemDocIndex(aspec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);

        Set<JsonDoc> results=index.find(new CompositeLookupSpec(new LookupSpec[] {new RangeLookupSpec(spec1,"field1:10","field1:15"),
                                                                                  new ValueLookupSpec(spec2,"field2:10")}));
        Assert.assertEquals(1,results.size());
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:10"));
    }

    @Test
    public void twoPrefixLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // indexeing using field1 and field2
        SimpleKeySpec spec1=new SimpleKeySpec(qfi(md,"field1","field1"));
        SimpleKeySpec spec2=new SimpleKeySpec(qfi(md,"field2","field2"));
        CompositeKeySpec aspec=new CompositeKeySpec(new KeySpec[] {spec1,spec2});
        MemDocIndex index=new MemDocIndex(aspec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);

        Set<JsonDoc> results=index.find(new CompositeLookupSpec(new LookupSpec[] {new PrefixLookupSpec(spec1,"FIELD1:1",true),
                                                                                  new ValueLookupSpec(spec2,"field2:10")}));
        Assert.assertEquals(1,results.size());
        Assert.assertTrue(results.stream().map(d->d.get(new Path("field1")).asText()).collect(Collectors.toSet()).contains("field1:10"));
    }

    @Test
    public void simpleArrayLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        // Find elemf1:
        SimpleKeySpec spec=new SimpleKeySpec(qfi(md,"field7.*.elemf1","field7.*.elemf1"));
        MemDocIndex index=new MemDocIndex(spec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);
        
        // Search
        Set<JsonDoc> results=index.find(new ValueLookupSpec(spec,"doc:10 elemf1:20"));
        Assert.assertEquals(1,results.size());
        Assert.assertEquals("field1:10",results.iterator().next().get(new Path("field1")).asText());
    }

    @Test
    public void doubleSimpleArrayLookupTest() throws Exception {
        EntityMetadata md=getMd("testMetadata.json");
        List<JsonDoc> docs=fill();

        QueryFieldInfo array=qfi(md,"field7","field7");
        SimpleKeySpec spec1=new SimpleKeySpec(qfi(md,"elemf1","field7.*.elemf1"));
        SimpleKeySpec spec2=new SimpleKeySpec(qfi(md,"elemf2","field7.*.elemf2"));
        ArrayKeySpec aspec=new ArrayKeySpec(array,new SimpleKeySpec[] {spec1,spec2});
        MemDocIndex index=new MemDocIndex(aspec);
        
        // Add all docs
        for(JsonDoc doc:docs)
            index.add(doc);

        // Search
        Set<JsonDoc> results=index.find(new ArrayLookupSpec(new LookupSpec[]{new ValueLookupSpec(spec1,"doc:2 elemf1:10"),
                                                                             new ValueLookupSpec(spec2,"doc:2 elemf2:10")}));
        Assert.assertEquals(1,results.size());
        Assert.assertEquals("field1:2",results.iterator().next().get(new Path("field1")).asText());
    }

}
