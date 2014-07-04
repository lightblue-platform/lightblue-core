package com.redhat.lightblue.metadata.rdbms;

public class RDBMSConfiguration {
    private Statement select;
    private Statement insert;
    private Statement update;
    private Statement delete;

    public Statement getSelect() {
        return select;
    }

    public void setSelect(Statement select) {
        this.select = select;
    }

    public Statement getInsert() {
        return insert;
    }

    public void setInsert(Statement insert) {
        this.insert = insert;
    }

    public Statement getUpdate() {
        return update;
    }

    public void setUpdate(Statement update) {
        this.update = update;
    }

    public Statement getDelete() {
        return delete;
    }

    public void setDelete(Statement delete) {
        this.delete = delete;
    }
}
