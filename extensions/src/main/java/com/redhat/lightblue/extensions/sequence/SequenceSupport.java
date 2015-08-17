package com.redhat.lightblue.extensions.sequence;

import com.redhat.lightblue.extensions.Extension;

/**
 * Sequence extension provides backend specific sequence generation capabilities.
 *
 * @author mpatercz
 *
 */
public interface SequenceSupport<T> extends Extension {

    /**
     *
     * @param backend e.g. mongo
     * @return
     */
    Sequence<T> getSequenceInstance(String backend);

}
