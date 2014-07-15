package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

public class IfPathEmpty extends  If {
    private String path1;
    public void setPath1(String path1) {
        this.path1 = path1;
    }

    public String getPath1() {
        return path1;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        p.putString(node,"path1",path1);
    }
}
