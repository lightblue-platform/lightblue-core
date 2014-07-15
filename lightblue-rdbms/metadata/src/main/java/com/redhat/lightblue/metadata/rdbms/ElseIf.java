package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

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

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        T eT = p.newNode();

        T iT = p.newNode();
        p.putObject(eT, "$if", iT);

        anIf.convert(p,lastArrayNode,iT);
        then.convert(p,lastArrayNode,eT); //it already add $then

        p.addObjectToArray(lastArrayNode,eT);
    }
}
