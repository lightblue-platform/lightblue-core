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
        this.conditional = conditional;
    }

    public String getConditional() {
        return conditional;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        p.putString(node,"path1",path1.toString());
        p.putString(node,"value2",value2);
        p.putString(node,"conditional",conditional);
    }
}
