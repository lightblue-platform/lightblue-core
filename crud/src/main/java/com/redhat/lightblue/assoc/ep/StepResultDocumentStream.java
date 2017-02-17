package com.redhat.lightblue.assoc.ep;

import java.util.Iterator;
import java.util.stream.Stream;

import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.DocumentStream;

/**
 * Interface between DocumentStream used in CRUDOperationContext, and StepResult
 */
public class StepResultDocumentStream implements DocumentStream<DocCtx> {

    private final Iterator<DocCtx> itr;

    public StepResultDocumentStream(StepResult<DocCtx> result) {
        this.itr=result.stream().iterator();
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }

    @Override
    public DocCtx next() {
        return itr.next();
    }

    @Override
    public void close() {}
}
