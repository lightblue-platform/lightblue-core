/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.crud.hystrix;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.crud.CrudManager;
import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.rest.crud.RestCrudConstants;
import com.redhat.lightblue.util.Error;

/**
 * Note that passing a Mediator in the constructor is optional. If not provided, it is fetched from CrudManager object.
 *
 * @author nmalik
 */
public abstract class AbstractRestCommand extends HystrixCommand<String> {
    protected static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private final Mediator mediator;

    public AbstractRestCommand(String groupKey, String commandKey, String threadPoolKey) {
        this(groupKey, commandKey, threadPoolKey, null);
    }

    /**
     *
     * @param groupKey REQUIRED
     * @param commandKey OPTIONAL defaults to groupKey value
     * @param threadPoolKey OPTIONAL defaults to groupKey value
     */
    public AbstractRestCommand(String groupKey, String commandKey, String threadPoolKey, Mediator mediator) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey == null ? groupKey : commandKey))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey == null ? groupKey : threadPoolKey)));
        this.mediator = mediator;
    }

    /**
     * Returns the mediator. If no mediator is set on the command uses CrudManager#getMediator() method.
     *
     * @return
     * @throws Exception
     */
    protected Mediator getMediator() throws Exception {
        if (null != mediator) {
            return mediator;
        } else {
            return CrudManager.getMediator();
        }
    }

    protected void validateReq(Request req, String entity, String version) {
        // If entity and/or version is not set in the request, this
        // code below sets it from the uri
        if (req.getEntityVersion() == null) {
            req.setEntityVersion(new EntityVersion());
        }
        if (req.getEntityVersion().getEntity() == null) {
            req.getEntityVersion().setEntity(entity);
        }
        if (req.getEntityVersion().getVersion() == null) {
            req.getEntityVersion().setVersion(version);
        }
        if (!req.getEntityVersion().getEntity().equals(entity)) {
            throw Error.get(RestCrudConstants.ERR_NO_ENTITY_MATCH, entity);
        }
        if (!req.getEntityVersion().getVersion().equals(version)) {
            throw Error.get(RestCrudConstants.ERR_NO_VERSION_MATCH, version);
        }
    }
}
