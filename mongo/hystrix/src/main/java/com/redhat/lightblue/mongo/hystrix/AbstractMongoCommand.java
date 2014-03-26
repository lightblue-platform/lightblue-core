/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;

/**
 *
 * @author nmalik
 */
public abstract class AbstractMongoCommand<T> extends HystrixCommand<T> {
    private final DBCollection collection;

    /**
     *
     * @param groupKey REQUIRED
     * @param commandKey OPTIONAL defaults to groupKey value
     * @param threadPoolKey OPTIONAL defaults to groupKey value
     * @param collection REQUIRED
     */
    public AbstractMongoCommand(String groupKey, String commandKey, String threadPoolKey, DBCollection collection) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey == null ? groupKey : commandKey))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey == null ? groupKey : threadPoolKey)));
        this.collection = collection;
    }

    protected DBCollection getDBCollection() {
        return collection;
    }
}
