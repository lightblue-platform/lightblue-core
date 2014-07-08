package com.redhat.lightblue.metadata.rdbms;

import java.util.ArrayList;
import java.util.List;

public class ElseIf {
    private List<If> ifs;
    private Then then;

    public ElseIf() {
        ifs = new ArrayList<>();
    }
}
