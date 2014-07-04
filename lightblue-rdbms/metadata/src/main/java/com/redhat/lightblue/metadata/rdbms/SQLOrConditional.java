package com.redhat.lightblue.metadata.rdbms;

public class SQLOrConditional {
    private Conditional conditional;
    private com.redhat.lightblue.metadata.rdbms.SQL SQL;

    public void setConditional(Conditional conditional) {
        this.conditional = conditional;
    }

    public Conditional getConditional() {
        return conditional;
    }

    public void setSQL(SQL SQL) {
        this.SQL = SQL;
    }

    public SQL getSQL() {
        return SQL;
    }
}
