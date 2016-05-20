package com.redhat.lightblue.assoc.ep;

import java.util.stream.Stream;

/**
 * Wraps a step result. Convenience class for other result implementations
 */
public class StepResultWrapper<T> implements StepResult<T> {

    protected final StepResult<T> wrapped;

    public StepResultWrapper(StepResult<T> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Stream<T> stream() {
        return wrapped.stream();
    }
}
