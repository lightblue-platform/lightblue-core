package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

import java.util.List;

public class Conditional  extends Expression {
    private If anIf;
    private Then then;
    private List<ElseIf> elseIfList;
    private Else anElse;

    public void setIf(If anIf) {
        this.anIf = anIf;
    }

    public If getIf() {
        return anIf;
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

    public Else getElse() {
        return anElse;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        T eT = p.newNode();

        T iT = p.newNode();
        anIf.convert(p,expressionsNode,iT);
        p.putObject(eT, "$if", iT);

        then.convert(p, expressionsNode, eT);

        if(!elseIfList.isEmpty()) {
            Object arri = p.newArrayField(eT, "$elseIf");
            for (ElseIf e : elseIfList) {
                e.convert(p, arri, eT);
            }
        }

        anElse.convert(p, expressionsNode, eT);

        p.addObjectToArray(expressionsNode,eT);
    }
}