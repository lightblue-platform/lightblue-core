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
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSUtilsMetadata;
import com.redhat.lightblue.metadata.rdbms.converter.SelectStmt;
import java.util.List;

public class ExecuteUpdateCommand<T> extends HystrixCommand<Void> {

    private final RDBMSContext<T> rdbmsContext;
    private List<SelectStmt> inputStmt;

    public ExecuteUpdateCommand(RDBMSContext<T> rdbmsContext) {
        this(rdbmsContext,null);
    }
    
    public ExecuteUpdateCommand(RDBMSContext rdbmsContext, List<SelectStmt> inputStmt) {
        this(rdbmsContext, inputStmt, null);       
    }
        
    /**
     * @param threadPoolKey OPTIONAL defaults to groupKey value
     */
    public ExecuteUpdateCommand(RDBMSContext<T> rdbmsContext, List<SelectStmt> inputStmt, String threadPoolKey) {    
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(ExecuteUpdateCommand.class.getSimpleName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(ExecuteUpdateCommand.class.getSimpleName()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey == null ? ExecuteUpdateCommand.class.getSimpleName() : threadPoolKey)));
        
        this.rdbmsContext = rdbmsContext;
        this.inputStmt = inputStmt;
    }

    /**
     * Unwrap hystrix exception
     */
    @Override
    public Void execute() {
        try {
            return super.execute();
        } catch (HystrixBadRequestException br) {
            throw (RuntimeException) br.getCause();
        } catch (RuntimeException x) {
            throw x;
        }
    }

    @Override
    protected Void run() {
        try {
            RDBMSUtilsMetadata.buildAllMappedList(rdbmsContext);
        } catch (RuntimeException x) {
            throw new HystrixBadRequestException("in " + getClass().getName(), x);
        }
        return null;
    }
}
