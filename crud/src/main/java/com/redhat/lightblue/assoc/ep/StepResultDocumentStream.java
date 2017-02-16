package com.redhat.lightblue.assoc.ep;

import java.util.Iterator;
import java.util.stream.Stream;

import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.DocumentStream;

/**
 * Interface between DocumentStream used in CRUDOperationContext, and StepResult
 */
public class StepResultDocumentStream implements DocumentStream<DocCtx> {

    private final StepResult<DocCtx> result;

    public StepResultDocumentStream(StepResult<DocCtx> result) {
        this.result=result;
    }

    @Override
    public Iterator<DocCtx> getDocuments() {
        return result.stream().iterator();
    }
}
