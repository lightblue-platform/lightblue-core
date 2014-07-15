package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

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

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        T eT = p.newNode();
        p.putString(eT,"iterateOverPath",iterateOverPath);
        Object o = p.newArrayField(eT, "expressions");
        for (Expression expression : expressions) {
            expression.convert(p,o);
        }
        p.addObjectToArray(expressionsNode, eT);
    }
}
