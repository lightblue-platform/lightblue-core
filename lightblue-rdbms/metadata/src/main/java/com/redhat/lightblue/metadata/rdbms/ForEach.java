package com.redhat.lightblue.metadata.rdbms;

import java.util.List;

public class ForEach extends Expression {
    private String iterateOverPath;
    private List<Expression> expressions;

    public void setIterateOverPath(String iterateOverPath) {
        this.iterateOverPath = iterateOverPath;
    }

    public String getIterateOverPath() {
        return iterateOverPath;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }
}
