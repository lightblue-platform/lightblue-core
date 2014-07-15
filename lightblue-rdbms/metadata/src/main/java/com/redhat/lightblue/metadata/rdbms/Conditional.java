package com.redhat.lightblue.metadata.rdbms;

import java.util.ArrayList;
import java.util.List;

public class Conditional  extends Expression {
    private If anIf;
    private Then then;
    private List<ElseIf> elseIfList;
    private Else anElse;

    public void setIf(If anIf) {
        this.anIf = anIf;
    }

    public If getAnIf() {
        return anIf;
    }

    public void setAnIf(If anIf) {
        this.anIf = anIf;
    }

    public void setThen(Then then) {
        this.then = then;
    }

    public Then getThen() {
        return then;
    }


    public void setElseIfList(List<ElseIf> elseIfList) {
        this.elseIfList = elseIfList;
    }

    public List<ElseIf> getElseIfList() {
        return elseIfList;
    }

    public void setElse(Else anElse) {
        this.anElse = anElse;
    }

    public Else getAnElse() {
        return anElse;
    }

    public void setAnElse(Else anElse) {
        this.anElse = anElse;
    }
}
