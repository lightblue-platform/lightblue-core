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

    static class A {
        public static int get(){return 1;}
    }

    static class B extends A {
        public static int get(){return 2;}
    }

    public static void main(String[] args) {
        B a = new B();
        System.out.println(a.get());
    }
}
