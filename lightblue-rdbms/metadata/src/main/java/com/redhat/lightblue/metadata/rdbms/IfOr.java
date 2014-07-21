package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

public class IfOr extends If<If> {
    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if(getConditions() == null || getConditions().size() < 2){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "$or/$any doesn't have enough conditionals");
        }
        Object eT = null;
        if(lastArrayNode == null){
            eT = p.newArrayField(node, "$or");
        } else {
            T iT = p.newNode();
            eT = p.newArrayField(iT, "$or");
            p.addObjectToArray(iT, eT);
            p.addObjectToArray(lastArrayNode, iT);
        }
        for(If i : getConditions()) {
            i.convert(p, eT, node);
        }
    }
}
