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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.Path;

public class IndexUsefulnessTest {

    private EntityMetadata getMD1() {
        EntityMetadata entityMetadata = new EntityMetadata("test");

        entityMetadata.getFields().addNew(new SimpleField("f1", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("f2", StringType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("f3", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("f4", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("f5", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("f6", IntegerType.TYPE));

        return entityMetadata;
    }

    @Test
    public void simpleIndexTest() {
        EntityMetadata md = getMD1();
        // Add indexes for some fields
        md.getEntityInfo().getIndexes().add(new Index(new IndexSortKey(new Path("f1"), false)));
        md.getEntityInfo().getIndexes().add(new Index(new IndexSortKey(new Path("f2"), false)));
        md.getEntityInfo().getIndexes().add(new Index(new IndexSortKey(new Path("f3"), false)));

        // Make sure usefulnes for single field works
        Assert.assertTrue(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f1")));
        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f2")));
        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f3")));

        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(1).isUseful(new Path("f1")));
        Assert.assertTrue(md.getEntityInfo().getIndexes().getIndexes().get(1).isUseful(new Path("f2")));
        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(1).isUseful(new Path("f3")));

        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(2).isUseful(new Path("f1")));
        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(2).isUseful(new Path("f2")));
        Assert.assertTrue(md.getEntityInfo().getIndexes().getIndexes().get(2).isUseful(new Path("f3")));
    }

    @Test
    public void compositeIndexTest() {
        EntityMetadata md = getMD1();
        // Add indexes for some fields
        md.getEntityInfo().getIndexes().add(new Index(new IndexSortKey(new Path("f1"), false),
                new IndexSortKey(new Path("f3"), true),
                new IndexSortKey(new Path("f5"), false)));

        // should be useful for f1, f3, and f5
        Assert.assertTrue(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f1")));
        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f2")));
        Assert.assertTrue(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f3")));
        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f4")));
        Assert.assertTrue(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f5")));
        Assert.assertFalse(md.getEntityInfo().getIndexes().getIndexes().get(0).isUseful(new Path("f6")));

        List<Path> fieldList = new ArrayList();
        fieldList.add(new Path("f1"));
        fieldList.add(new Path("f2"));
        fieldList.add(new Path("f3"));
        Map<Index, Set<Path>> ix = md.getEntityInfo().getIndexes().getUsefulIndexes(fieldList);
        Assert.assertEquals(1, ix.size());
        Set<Path> paths = ix.get(md.getEntityInfo().getIndexes().getIndexes().get(0));
        Assert.assertEquals(2, paths.size());
        Iterator<Path> itr = paths.iterator();
        Assert.assertEquals("f1", itr.next().toString());
        Assert.assertEquals("f3", itr.next().toString());
    }
}
