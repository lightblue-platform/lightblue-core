package com.redhat.lightblue.assoc.ep;

import java.util.stream.Stream;

import com.redhat.lightblue.crud.DocCtx;

/**
 * Decorator interface that gets a StepResult<ResultDocument> and return StepResult<DocCtx>
 */
public class MakeDocCtx implements StepResult<DocCtx> {

    private final StepResult<ResultDocument> result;

    public MakeDocCtx(StepResult<ResultDocument> result) {
        this.result=result;
    }

    @Override
    public Stream<DocCtx> stream() {
        return result.stream().map(d->new DocCtx(d.getDoc()));
    }
}
