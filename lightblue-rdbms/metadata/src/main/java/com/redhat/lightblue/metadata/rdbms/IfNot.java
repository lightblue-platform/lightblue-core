package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

public class IfNot extends If<If> {
    @Override
    public<T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if(getConditions() == null || getConditions().size() != 1){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "$not doesn't have just one conditional");
        }
        If o = getConditions().get(0);
        T eT = p.newNode();
        o.convert(p,lastArrayNode,eT);
        p.putObject(node,"$not",eT);
    }
}
