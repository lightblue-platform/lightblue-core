package com.redhat.lightblue.hystrix.rdbms;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.redhat.lightblue.common.rdbms.RDBMSContext;
import com.redhat.lightblue.common.rdbms.RDBMSUtils;

import javax.sql.DataSource;
import java.util.List;

//TODO
public class QueryCommand<T> extends HystrixCommand<List<T>> {

    private final RDBMSContext<T> rdbmsContext;

    /**
     * @param threadPoolKey OPTIONAL defaults to groupKey value
     */
    public QueryCommand(String threadPoolKey,RDBMSContext<T> rdbmsContext) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(QueryCommand.class.getSimpleName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(QueryCommand.class.getSimpleName()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey == null ? QueryCommand.class.getSimpleName() : threadPoolKey)));
        this.rdbmsContext = rdbmsContext;
    }

    /**
     * Unwrap hystrix exception
     */
    @Override
    public List<T> execute() {
        try {
            return super.execute();
        } catch (HystrixBadRequestException br) {
            throw (RuntimeException)br.getCause();
        } catch (RuntimeException x) {
            throw x;
        }
    }

    @Override
    protected List<T> run() {
        try {
            RDBMSUtils rdbmsUtils = new RDBMSUtils();
            DataSource dataSource = rdbmsUtils.getDataSource(rdbmsContext);
            //....
            return rdbmsContext.getResultList();
        } catch (RuntimeException x) {
            throw new HystrixBadRequestException("in "+getClass().getName(),x);
        }
    }
}
