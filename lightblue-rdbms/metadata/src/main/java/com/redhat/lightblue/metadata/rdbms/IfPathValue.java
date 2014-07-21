package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Path;

public class IfPathValue extends If {
    private Path path1;
    private String value2;
    private String conditional;

    public void setPath1(Path path1) {
        this.path1 = path1;
    }

    public Path getPath1() {
        return path1;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getValue2() {
        return value2;
    }

    public void setConditional(String conditional) {
        if(!ConditionalOperators.check(conditional)){
            throw new IllegalStateException("Not a valid conditional '" +conditional+"'. Valid ConditionalOperators:"+ ConditionalOperators.getValues());
        }
        this.conditional = conditional;
    }

    public String getConditional() {
        return conditional;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if(path1 == null || path1.isEmpty()){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No path1 informed");
        }
        if(conditional == null || conditional.isEmpty()){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No conditional informed");
        }
        if(value2 == null || value2.isEmpty()){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No value2 informed");
        }
        T s = p.newNode();

        p.putString(s,"path1",path1.toString());
        p.putString(s,"value2",value2);
        p.putString(s,"conditional",conditional);

        p.putObject(node,"$path-check-value",s);
    }
}
