package com.redhat.lightblue.metadata.rdbms;

public class Then {
    private Statement statement;
    private String loopOperator;

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public String getLoopOperator() {
        return loopOperator;
    }

    public void setLoopOperator(String loopOperator) {
        this.loopOperator = loopOperator;
    }
}
