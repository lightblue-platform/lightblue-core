package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import org.junit.Test;

public class SimpleConverterTest {
    

    @Test
    public void testConvert() {
        SimpleConverter instance = new SimpleConverterImpl();
        instance.convert(null, null);
    }

    public class SimpleConverterImpl implements SimpleConverter {

        public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        }
    }
    
}
