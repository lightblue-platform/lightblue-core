package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

public class IfPathPath extends If {
    private String path1;
    private String path2;
    private String conditional;

    public void setPath1(String path1) {
        this.path1 = path1;
    }

    public String getPath1() {
        return path1;
    }

    public void setPath2(String path2) {
        this.path2 = path2;
    }

    public String getPath2() {
        return path2;
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
        p.putString(node,"path2",path2);
        p.putString(node,"conditional",conditional);
    }
}
