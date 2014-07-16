package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Path;

import java.util.List;

public class IfPathValues extends If{
    private Path path1;
    private List<String> values2;
    private String conditional;

    public void setPath1(Path path1) {
        this.path1 = path1;
    }

    public Path getPath1() {
        return path1;
    }

    public void setValues2(List<String> values2) {
        this.values2 = values2;
    }

    public List<String> getValues2() {
        return values2;
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
        Object arri = p.newArrayField(node, "value2");
        for(String s : values2){
            p.addStringToArray(arri,s);
        }
        p.putString(node,"conditional",conditional);
    }
}
