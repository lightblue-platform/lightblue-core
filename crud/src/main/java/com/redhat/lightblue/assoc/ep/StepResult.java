package com.redhat.lightblue.assoc.ep;

import java.util.stream.Stream;

public interface StepResult<T> {
    /**
     * Returns a stream of results. Once the stream processing is
     * complete, stream is closed, and should not be used. The caller
     * can call stream() again to get a new stream.
     */
    Stream<T> stream();    
}
