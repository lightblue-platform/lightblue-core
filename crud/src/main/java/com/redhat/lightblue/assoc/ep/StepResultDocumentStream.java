package com.redhat.lightblue.assoc.ep;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.DocumentStream;

/**
 * Interface between DocumentStream used in CRUDOperationContext, and StepResult
 */
public class StepResultDocumentStream implements DocumentStream<DocCtx> {

    private final Iterator<DocCtx> itr;
    private final ArrayList<Consumer<DocCtx>> listeners=new ArrayList<>();

    public StepResultDocumentStream(StepResult<DocCtx> result) {
        this.itr=result.stream().iterator();
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }

    @Override
    public DocCtx next() {
        DocCtx doc=itr.next();
        for(Consumer<DocCtx> c:listeners)
            c.accept(doc);
        return doc;
    }

    @Override
    public void close() {}

    @Override
    public void forEach(Consumer<DocCtx> listener) {
        listeners.add(listener);
    }
}
