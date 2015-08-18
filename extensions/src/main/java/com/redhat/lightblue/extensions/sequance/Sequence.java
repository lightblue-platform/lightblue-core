package com.redhat.lightblue.extensions.sequance;

import java.math.BigInteger;

import com.redhat.lightblue.metadata.SimpleField;

/**
 * TODO: Does SimpleField have all the info required by the backend to manage sequences?
 *
 * @author mpatercz
 *
 */
public interface Sequence {

    public void createSequence(SimpleField field);

    public void removeSequence(SimpleField field);

    /**
     * Generate next value in sequance. This should be handled automatically by the backend on insert.
     *
     * @param field
     * @return
     */
    public BigInteger nextInSequence(SimpleField field);

}
