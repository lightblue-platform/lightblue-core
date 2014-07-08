package com.redhat.lightblue.metadata.rdbms;

import java.util.ArrayList;
import java.util.List;

public class DynamicConditional {
    private List<If> ifs;
    private Then then;
    private ElseIf elseIf;
    private Else lastElse;

    public DynamicConditional() {
        ifs = new ArrayList<>();
    }

    public List<If> getIfs() {
        return ifs;
    }

    public void setIfs(List<If> ifs) {
        this.ifs = ifs;
    }

    public Then getThen() {
        return then;
    }

    public void setThen(Then then) {
        this.then = then;
    }

    public ElseIf getElseIf() {
        return elseIf;
    }

    public void setElseIf(ElseIf elseIf) {
        this.elseIf = elseIf;
    }

    public Else getLastElse() {
        return lastElse;
    }

    public void setLastElse(Else lastElse) {
        this.lastElse = lastElse;
    }
}
