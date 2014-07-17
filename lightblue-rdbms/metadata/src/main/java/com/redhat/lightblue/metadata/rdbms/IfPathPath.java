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
        if(ConditionalOperators.check(conditional)){
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
        if(path2 == null || path2.isEmpty()){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No path2 informed");
        }
        T s = p.newNode();

        p.putString(s,"path1",path1.toString());
        p.putString(s,"path2",path2.toString());
        p.putString(s,"conditional",conditional);

        p.putObject(node,"$path-check-path",s);
    }
}
