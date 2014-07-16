package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Path;

public class IfPathEmpty extends  If {
    private Path path1;
    public void setPath1(Path path1) {
        this.path1 = path1;
    }

    public Path getPath1() {
        return path1;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        p.putString(node,"path1",path1.toString());
    }
}
