package com.redhat.lightblue.extensions.sequence;

import com.redhat.lightblue.metadata.SimpleField;

/**
 * Sequence generation interface, to be implemented by the backend. Can return any value, but likely
 * an integer is going to be used.
 *
 * @author mpatercz
 *
 */
public interface Sequence<T> {

    /**
     * Generate next value in sequence. This should be handled automatically by the backend on insert.
     *
     * @param field
     * @return
     */
    public T nextInSequence(SimpleField field);

}
