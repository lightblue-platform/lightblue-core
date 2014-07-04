package com.redhat.lightblue.metadata.rdbms;

import java.util.List;

/**
 * Created by lcestari on 7/3/14.
 */
public class Statement {
    private List<SQLOrConditional> sQLOrConditionalList;
    private Bindings bindings;

    public void setsQLOrConditionalList(List<SQLOrConditional> sQLOrConditionalList) {
        this.sQLOrConditionalList = sQLOrConditionalList;
    }

    public List<SQLOrConditional> getsQLOrConditionalList() {
        return sQLOrConditionalList;
    }

    public void setBindings(Bindings bindings) {
        this.bindings = bindings;
    }

    public Bindings getBindings() {
        return bindings;
    }
}
