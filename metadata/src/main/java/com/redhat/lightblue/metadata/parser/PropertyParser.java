package com.redhat.lightblue.metadata.parser;

import java.util.Map;

public abstract class  PropertyParser<T> implements Parser<T,Object> {
    public void parseProperty(MetadataParser<T> MetadataParser, String name, Map<String, Object> properties, T objectProperty){
        final Object obj = parse(name, MetadataParser, objectProperty);
        properties.put(name,obj);
    }
}
