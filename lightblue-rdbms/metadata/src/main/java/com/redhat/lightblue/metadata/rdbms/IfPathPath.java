package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Path;

public class IfPathPath extends If {
    private Path path1;
    private Path path2;
    private String conditional;

    public void setPath1(Path path1) {
        this.path1 = path1;
    }

    public Path getPath1() {
        return path1;
    }

    public void setPath2(Path path2) {
        this.path2 = path2;
    }

    public Path getPath2() {
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
        p.putString(node,"path1",path1.toString());
        p.putString(node,"path2",path2.toString());
        p.putString(node,"conditional",conditional);
    }
}
