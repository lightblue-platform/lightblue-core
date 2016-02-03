/**
 *
 */
package com.redhat.lightblue.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.Path;

/**
 * @author bvulaj
 *
 */
public class IndexCaseInsensitiveTest {

    private EntityMetadata emd;

    @Before
    public void setup() {
        emd = new EntityMetadata("test");
        emd.getFields().addNew(new SimpleField("f1", StringType.TYPE));
        emd.getFields().addNew(new SimpleField("f2", StringType.TYPE));
        emd.getFields().addNew(new SimpleField("f3", StringType.TYPE));
        emd.getFields().addNew(new SimpleField("f4", StringType.TYPE));
        emd.getFields().addNew(new SimpleField("f5", StringType.TYPE));
        emd.getFields().addNew(new SimpleField("f6", StringType.TYPE));

        emd.getEntityInfo().getIndexes().add(new Index(new IndexSortKey(new Path("f1"), false, true)));
        emd.getEntityInfo().getIndexes().add(new Index(new IndexSortKey(new Path("f2"), false, false)));
        emd.getEntityInfo().getIndexes().add(new Index(new IndexSortKey(new Path("f3"), false, true)));
    }

    @Test
    public void testFindCaseInsensIndexes() {
        assertEquals(2, emd.getEntityInfo().getIndexes().getCaseInsensitiveIndexes().size());

        assertEquals("f1", emd.getEntityInfo().getIndexes().getCaseInsensitiveIndexes().get(0).getField().toString());
        assertEquals("f3", emd.getEntityInfo().getIndexes().getCaseInsensitiveIndexes().get(1).getField().toString());
    }

    @Test
    public void testIsCaseInsensIndex() {
        assertTrue(emd.getEntityInfo().getIndexes().isCaseInsensitiveKey(new Path("f1")));
        assertFalse(emd.getEntityInfo().getIndexes().isCaseInsensitiveKey(new Path("f2")));
        assertTrue(emd.getEntityInfo().getIndexes().isCaseInsensitiveKey(new Path("f3")));
        assertFalse(emd.getEntityInfo().getIndexes().isCaseInsensitiveKey(new Path("f4")));
    }

}
