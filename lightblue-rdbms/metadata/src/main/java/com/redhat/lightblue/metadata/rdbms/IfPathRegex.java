package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Path;

public class IfPathRegex extends If {
    private Path path;
    private String regex;
    private boolean caseInsensitive;
    private boolean multiline;
    private boolean extended;
    private boolean dotall;

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setDotall(boolean dotall) {
        this.dotall = dotall;
    }

    public boolean isDotall() {
        return dotall;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if(path == null || path.isEmpty()){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No path1 informed");
        }
        if(regex == null || regex.isEmpty()){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No conditional informed");
        }
        p.putString(node,"path",path.toString());
        p.putString(node,"regex",regex);
        if(!caseInsensitive) { // different than the default value
            p.putString(node, "case_insensitive", str(caseInsensitive));
        }
        if(!multiline) {
            p.putString(node, "multiline", str(multiline));
        }
        if(!multiline) {
            p.putString(node, "extended", str(extended));
        }
        if(!multiline) {
            p.putString(node, "dotall", str(dotall));
        }
    }

    //to make more readable
    private String str(boolean b){
        return Boolean.toString(b);
    }
}
