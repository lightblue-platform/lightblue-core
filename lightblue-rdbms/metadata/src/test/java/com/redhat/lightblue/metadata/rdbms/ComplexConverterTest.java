package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import org.junit.Test;

public class ComplexConverterTest {

    @Test
    public void testConvert() {
        ComplexConverter instance = new ComplexConverterImpl();
        instance.convert(null, null, null);
    }

    public class ComplexConverterImpl implements ComplexConverter {

        public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        }
    }
    
}
