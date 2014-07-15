package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

public class IfOr extends If<If> {
    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        T eT = p.newNode();
        for(If i : getConditions()) {
            i.convert(p, lastArrayNode, eT);
        }
        p.putObject(node, "$or", eT);
    }
}
