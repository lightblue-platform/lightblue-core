package com.redhat.lightblue.metadata.rdbms;

import java.util.List;

public class For extends Expression {
    private int loopTimes;
    private String loopCounterVariableName;
    private List<Expression> expressions;

    public void setLoopTimes(int loopTimes) {
        this.loopTimes = loopTimes;
    }

    public int getLoopTimes() {
        return loopTimes;
    }

    public void setLoopCounterVariableName(String loopCounterVariableName) {
        this.loopCounterVariableName = loopCounterVariableName;
    }

    public String getLoopCounterVariableName() {
        return loopCounterVariableName;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }
}
