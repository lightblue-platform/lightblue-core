package com.redhat.lightblue.metadata.rdbms;

import java.util.List;

public class Bindings {
    private List<InOut> inList;
    private List<InOut> outList;

    public void setInList(List<InOut> inList) {
        this.inList = inList;
    }

    public List<InOut> getInList() {
        return inList;
    }

    public void setOutList(List<InOut> outList) {
        this.outList = outList;
    }

    public List<InOut> getOutList() {
        return outList;
    }
}
