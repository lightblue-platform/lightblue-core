package com.redhat.lightblue.metadata.parser;

import junit.framework.Assert;

import org.junit.Test;

import com.redhat.lightblue.metadata.MetadataParser;

public class ParserRegistryTest {

    class TestParser implements Parser<Object, Object> {

        public Object parse(MetadataParser<Object> p, Object node) {
            return null;
        }

        public void convert(MetadataParser<Object> p, Object emptyNode, Object object) {
        }
    }

    @Test
    public void get() {
        ParserRegistry<Object, Object> reg = new ParserRegistry<Object, Object>();

        TestParser parser = new TestParser();

        String name = "foo";

        reg.add(name, parser);

        Assert.assertEquals(parser, reg.get(name));
    }
}
