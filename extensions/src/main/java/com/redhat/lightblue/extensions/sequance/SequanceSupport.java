package com.redhat.lightblue.extensions.sequance;

import com.redhat.lightblue.extensions.Extension;

/**
 * Sequence extension provides backend specific sequence number generation capabilities.
 *
 * @author mpatercz
 *
 */
public interface SequanceSupport extends Extension {

    Sequance getSequanceInstance();

}
