package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.util.Path;

public class InOut {
    private String column;
    private Path path;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
