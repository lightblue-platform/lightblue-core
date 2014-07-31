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
package com.redhat.lightblue.hystrix.rdbms;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import java.util.List;

//TODO
public class ExecuteUpdateCommand<T> extends HystrixCommand<List<T>> {

    //private final RDBMSContext<T> rdbmsContext;

    /**
     * @param threadPoolKey OPTIONAL defaults to groupKey value
     */
    //public ExecuteUpdateCommand(String threadPoolKey, RDBMSContext<T> rdbmsContext) {
    public ExecuteUpdateCommand(String threadPoolKey) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(ExecuteUpdateCommand.class.getSimpleName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(ExecuteUpdateCommand.class.getSimpleName()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey == null ? ExecuteUpdateCommand.class.getSimpleName() : threadPoolKey)));
        //this.rdbmsContext = rdbmsContext;
    }

    /**
     * Unwrap hystrix exception
     */
    @Override
    public List<T> execute() {
        try {
            return super.execute();
        } catch (HystrixBadRequestException br) {
            throw (RuntimeException) br.getCause();
        } catch (RuntimeException x) {
            throw x;
        }
    }

    @Override
    protected List<T> run() {
        try {
            return null;
            /*
            RDBMSUtils rdbmsUtils = new RDBMSUtils();
            rdbmsUtils.getDataSource(rdbmsContext);
            rdbmsUtils.getConnection(rdbmsContext);
            rdbmsUtils.getStatement(rdbmsContext);
            rdbmsUtils.buildMappedList(rdbmsContext);
            return rdbmsContext.getResultList();
            */
        } catch (RuntimeException x) {
            throw new HystrixBadRequestException("in " + getClass().getName(), x);
        }
    }
}
