package com.redhat.lightblue.extensions.sequance;

import com.redhat.lightblue.metadata.SimpleField;

/**
 * TODO: Does SimpleField have all the info required by the backend to manage sequences?
 *
 * @author mpatercz
 *
 */
public interface Sequance {

    public void createSequance(SimpleField field);

    public void removeSequance(SimpleField field);

    /**
     * Generate next value in sequance. This should be handled automatically by the backend on insert.
     *
     * @param field
     * @return
     */
    public Long nextInSequance(SimpleField field);

}
