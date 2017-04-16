package com.redhat.lightblue.config;

/**
 * An interface indicating that an implementing class wants an instance of {@link MediatorClient}
 * when it is instantiated.
 *
 * @author dcrissman
 */
public interface MediatorClientAware {

    void setMediatorClient(MediatorClient mediatorClient);

}
