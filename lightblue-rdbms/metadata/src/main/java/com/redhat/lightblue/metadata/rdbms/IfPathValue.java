package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

public class IfPathValue extends If {
    private String path1;
    private String value2;
    private String conditional;

    public void setPath1(String path1) {
        this.path1 = path1;
    }

    public String getPath1() {
        return path1;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getValue2() {
        return value2;
    }

    public void setConditional(String conditional) {
        this.conditional = conditional;
    }

    public String getConditional() {
        return conditional;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        p.putString(node,"path1",path1);
        p.putString(node,"value2",value2);
        p.putString(node,"conditional",conditional);
    }
}
