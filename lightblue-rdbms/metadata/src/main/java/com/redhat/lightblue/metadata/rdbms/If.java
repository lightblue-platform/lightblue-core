package com.redhat.lightblue.metadata.rdbms;


public class If {
    private String conditional;
    private String variable1;
    private String variable2;
    private String logicalOperatorNext;

    public String getConditional() {
        return conditional;
    }

    public void setConditional(String conditional) {
        this.conditional = conditional;
    }

    public String getVariable1() {
        return variable1;
    }

    public void setVariable1(String variable1) {
        this.variable1 = variable1;
    }

    public String getVariable2() {
        return variable2;
    }

    public void setVariable2(String variable2) {
        this.variable2 = variable2;
    }

    public String getLogicalOperatorNext() {
        return logicalOperatorNext;
    }

    public void setLogicalOperatorNext(String logicalOperatorNext) {
        this.logicalOperatorNext = logicalOperatorNext;
    }
}
