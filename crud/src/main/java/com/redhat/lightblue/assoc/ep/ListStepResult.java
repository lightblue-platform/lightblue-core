package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.stream.Stream;

/**
 * A step result backed by a list
 */
public class ListStepResult<T> implements StepResult<T> {

    protected List<T> list;

    public ListStepResult(List<T> list) {
        this.list=list;
    }

    protected ListStepResult() {}

    @Override
    public Stream<T> stream() {
        return list.stream();
    }
}
