package com.redhat.lightblue.metadata.rdbms;

import java.util.List;

public class Operation {
    private List<Expression> expressionList;
    private Bindings bindings;

    public void setExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    public void setBindings(Bindings bindings) {
        this.bindings = bindings;
    }

    public Bindings getBindings() {
        return bindings;
    }
}
