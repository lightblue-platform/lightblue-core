package com.redhat.lightblue.metadata.rdbms;

import java.util.ArrayList;
import java.util.List;

public class ElseIf implements ComplexConverter {


    private If anIf;
    private Then then;

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
}
