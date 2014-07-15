package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

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

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        T eT = p.newNode();
        p.putString(eT,"loopTimes",Integer.toString(loopTimes));
        p.putString(eT,"loopCounterVariableName",loopCounterVariableName);
        Object o = p.newArrayField(eT, "expressions");
        for (Expression expression : expressions) {
            expression.convert(p,o);
        }
        p.addObjectToArray(expressionsNode, eT);
    }
}
