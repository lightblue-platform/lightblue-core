package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.parser.PropertyParser;

public class RDBMSPropertyParserImpl<T> extends PropertyParser<T> {
    @Override
    public Object parse(String name, MetadataParser<T> p, T node) {
        return null;
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, Object object) {

    }
}
