package com.redhat.lightblue.assoc.ep;

import java.util.stream.Stream;

/**
 * The result type returned from steps. Contains stream() to return
 * the result stream.
 */
public interface StepResult<T> {

    public static StepResult EMPTY=new StepResult() {
            @Override
            public Stream stream() {return Stream.empty();}
        };
    
    /**
     * Returns a stream of results. Once the stream processing is
     * complete, stream is closed, and should not be used. The caller
     * can call stream() again to get a new stream.
     */
    Stream<T> stream();    
}
