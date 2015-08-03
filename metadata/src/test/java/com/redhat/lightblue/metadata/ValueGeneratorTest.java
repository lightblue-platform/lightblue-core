package com.redhat.lightblue.metadata;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.ValueGenerator.ValueGeneratorType;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ValueGeneratorTest extends AbstractJsonNodeTest {

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
        extensions.registerDataStoreParser("empty", new TestDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        return parser.parseEntityMetadata(node);
    }

    @Test
    public void parseTest() throws Exception {
        EntityMetadata md = getMd("metadata-json-schema-test-valid/schema-test-metadata-object-valueGenerator.json");

        ValueGenerator vg = md.getEntityInfo().getValueGenerators().getValueGenerator("testSeq");
        Assert.assertEquals("testSeq", vg.getName());
        Assert.assertEquals(ValueGeneratorType.IntSequance, vg.getType());
        Assert.assertEquals(2, vg.getProperties().size());
        Assert.assertEquals("10000", vg.getProperties().get("min"));
        Assert.assertEquals("99999", vg.getProperties().get("max"));
    }

}
