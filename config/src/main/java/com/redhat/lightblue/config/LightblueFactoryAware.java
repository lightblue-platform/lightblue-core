package com.redhat.lightblue.config;

/**
 * An interface indicating that an implementing class has knowledge of the {@link LightblueFactory}
 * that it is associated with.
 *
 * @author dcrissman
 */
public interface LightblueFactoryAware {

    void setLightblueFactory(LightblueFactory factory);

}
