package com.redhat.lightblue.metadata.rdbms;

public class SQLOrConditional {
    private DynamicConditional dynamicConditional;
    private com.redhat.lightblue.metadata.rdbms.SQL SQL;

    public void setDynamicConditional(DynamicConditional dynamicConditional) {
        this.dynamicConditional = dynamicConditional;
    }

    public DynamicConditional getDynamicConditional() {
        return dynamicConditional;
    }

    public void setSQL(SQL SQL) {
        this.SQL = SQL;
    }

    public SQL getSQL() {
        return SQL;
    }
}
