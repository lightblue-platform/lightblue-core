/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import com.mongodb.MongoSocketException;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;

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

    /**
     * Unwrap hystrix exception
     */
    @Override
    public T execute() {
        try {
            return super.execute();
        } catch (HystrixBadRequestException br) {
            throw (RuntimeException)br.getCause();
        } catch (RuntimeException x) {
            throw x;
        }
    }

    @Override
    protected T run() {
        try {
            return runMongoCommand();
        } catch (MongoSocketException mse) {
            throw mse;
        } catch (RuntimeException x) {
            throw new HystrixBadRequestException("in "+getClass().getName(),x);
        }
    }

    protected abstract T runMongoCommand();
}
