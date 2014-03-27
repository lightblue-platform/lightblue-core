/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata.hystrix;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.Metadata;

/**
 * Note that passing a Metadata in the constructor is optional. If not provided, it is fetched from MetadataManager
 * object.
 *
 * @author nmalik
 */
public abstract class AbstractRestCommand extends HystrixCommand<String> {
    protected static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private final Metadata metadata;

    /**
     *
     * @param groupKey REQUIRED
     * @param commandKey OPTIONAL defaults to groupKey value
     * @param threadPoolKey OPTIONAL defaults to groupKey value
     */
    public AbstractRestCommand(String groupKey, String commandKey, String threadPoolKey, Metadata metadata) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey == null ? groupKey : commandKey))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey == null ? groupKey : threadPoolKey)));
        this.metadata = metadata;
    }

    /**
     * Returns the metadata. If no metadata is set on the command uses MetadataManager#getMetadata() method.
     *
     * @return
     * @throws Exception
     */
    protected Metadata getMetadata() throws Exception {
        if (null != metadata) {
            return metadata;
        } else {
            return MetadataManager.getMetadata();
        }
    }
}
