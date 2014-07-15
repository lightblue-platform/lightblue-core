package com.redhat.lightblue.metadata.rdbms;

import java.util.List;

public class Then {

    private String loopOperator;
    private List<Expression> expressions;

    public void setLoopOperator(String loopOperator) {
        if(LoopOperators.check(loopOperator)){
            throw new IllegalStateException("Not a valid loop operator '" +loopOperator+"'. Valid Operators:"+ LoopOperators.getValues());
        }
        this.loopOperator = loopOperator;
    }

    public String getLoopOperator() {
        return loopOperator;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }
}
