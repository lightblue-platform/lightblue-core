package com.redhat.lightblue.assoc.ep;

import java.util.stream.Stream;
import java.util.stream.Collectors;

public class CachingStepResult<T> extends ListStepResult<T> {

    private StepResult<T> prevResult;
    
    public CachingStepResult(StepResult<T> r) {
        if(r instanceof CachingStepResult ||
           r instanceof ListStepResult)
            prevResult=r;
        else
            list=r.stream().collect(Collectors.toList());
    }
    
    @Override
    public Stream<T> stream() {
        return prevResult==null?list.stream():prevResult.stream();
    }
}
