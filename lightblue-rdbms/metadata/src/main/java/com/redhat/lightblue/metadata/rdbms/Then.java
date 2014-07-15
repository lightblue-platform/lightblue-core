package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

import java.util.List;

public class Then implements ComplexConverter{

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

    public String getName(){
        return "$then";
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if(loopOperator != null) {
            p.putString(node,getName(),loopOperator);
        }else{
            Object arri = p.newArrayField(node, getName());
            for(Expression s : expressions){
                s.convert(p,expressions);
            }
        }

    }
}
