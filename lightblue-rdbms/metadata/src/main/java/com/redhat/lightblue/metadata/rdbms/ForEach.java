package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Path;

import java.util.List;

public class ForEach extends Expression {
    private Path iterateOverPath;
    private List<Expression> expressions;

    public void setIterateOverPath(Path iterateOverPath) {
        this.iterateOverPath = iterateOverPath;
    }

    public Path getIterateOverPath() {
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
        p.putString(eT,"iterateOverPath",iterateOverPath.toString());
        Object o = p.newArrayField(eT, "expressions");
        for (Expression expression : expressions) {
            expression.convert(p,o);
        }
        p.addObjectToArray(expressionsNode, eT);
    }
}
