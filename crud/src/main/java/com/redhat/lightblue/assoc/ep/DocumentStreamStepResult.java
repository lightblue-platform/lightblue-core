package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.Spliterators;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.redhat.lightblue.crud.DocumentStream;

/**
 * A step result backed by a document stream
 */
public class DocumentStreamStepResult<T> implements StepResult<T> {

    protected DocumentStream<T> stream;

    public DocumentStreamStepResult(DocumentStream<T> stream) {
        this.stream=stream;
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(stream.getDocuments(),Spliterator.IMMUTABLE),false);
    }
}
