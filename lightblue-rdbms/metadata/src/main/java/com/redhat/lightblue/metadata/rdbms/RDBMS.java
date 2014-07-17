package com.redhat.lightblue.metadata.rdbms;

public class RDBMS {

    private Operation delete;
    private Operation fetch;
    private Operation insert;
    private Operation save;
    private Operation update;

    public void setDelete(Operation delete) {
        this.delete = delete;
    }

    public Operation getDelete() {
        return delete;
    }

    public void setFetch(Operation fetch) {
        this.fetch = fetch;
    }

    public Operation getFetch() {
        return fetch;
    }

    public void setInsert(Operation insert) {
        this.insert = insert;
    }

    public Operation getInsert() {
        return insert;
    }

    public void setSave(Operation save) {
        this.save = save;
    }

    public Operation getSave() {
        return save;
    }

    public void setUpdate(Operation update) {
        this.update = update;
    }

    public Operation getUpdate() {
        return update;
    }
}
