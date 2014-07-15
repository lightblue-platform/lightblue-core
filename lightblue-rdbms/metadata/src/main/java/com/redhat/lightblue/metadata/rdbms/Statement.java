package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

public class Statement extends Expression {

    private String datasource;
    private String SQL;
    private String type;

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setSQL(String SQL) {
        this.SQL = SQL;
    }

    public String getSQL() {
        return SQL;
    }

    public void setType(String type) {
        if(TypeOperators.check(type)){
            throw new IllegalStateException("Not a valid type of SQL operation '" +type+"'. Valid types:"+ TypeOperators.getValues());
        }
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        T eT = p.newNode();
        p.putString(eT,"datasource",datasource);
        p.putString(eT,"sql",SQL);
        p.putString(eT,"type",type);
        p.addObjectToArray(expressionsNode, eT);
    }
}
