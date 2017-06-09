package com.redhat.lightblue.util.stopwatch;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class StopWatchTest {

    class StopWatched {

        @StopWatch(loggerName="logger", warnThresholdMS=100)
        public void watchedMethodExplicitThreshold(int executionTimeMS) {
            try {
                Thread.sleep(executionTimeMS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @StopWatch(loggerName="logger")
        public void watchedMethod(int executionTimeMS) {
            try {
                Thread.sleep(executionTimeMS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    class MockStopWatchLogger extends StopWatchLogger {

        List<String> logEntries = new ArrayList<>();

        @Override
        public void warn(String loggerName, String message) {
            logEntries.add("WARN "+loggerName+": "+message);
        }

        @Override
        public void debug(String loggerName, String message) {
            logEntries.add("DEBUG "+loggerName+": "+message);
        }

        @Override
        public boolean isDebugEnabled(String loggerName) {
            return false;
        }

    }

    MockStopWatchLogger logger;

    @Before
    public void before() {
        logger = new MockStopWatchLogger();
        StopWatchAspect.logger = logger;
        StopWatchAspect.clear();
    }

    @Test
    public void testExplicitExecutionTimeThreshold() {
        System.setProperty("stopwatch.enabled", "true");

        StopWatched w = new StopWatched();

        w.watchedMethodExplicitThreshold(50);

        Assert.assertEquals(0,logger.logEntries.size());

        w.watchedMethodExplicitThreshold(150);

        Assert.assertEquals(1,logger.logEntries.size());
        Assert.assertTrue(logger.logEntries.get(0).startsWith("WARN logger: Long call=StopWatched#watchedMethodExplicitThreshold(150) executionTimeMS=1"));
    }

    @Test
    public void testGlobalExecutionTimeThreshold() {
        System.setProperty("stopwatch.enabled", "true");
        System.setProperty("stopwatch.globalWarnThresholdMS", "100");

        StopWatched w = new StopWatched();

        w.watchedMethod(50);

        Assert.assertEquals(0,logger.logEntries.size());

        w.watchedMethod(150);

        Assert.assertEquals(1,logger.logEntries.size());
        Assert.assertTrue(logger.logEntries.get(0).startsWith("WARN logger: Long call=StopWatched#watchedMethod(150) executionTimeMS=1"));
    }

    @Test
    public void testExplicitThresholdTakesPrecedenseOverGlobal() {
        System.setProperty("stopwatch.enabled", "true");
        System.setProperty("stopwatch.globalWarnThresholdMS", "5");

        StopWatched w = new StopWatched();

        w.watchedMethodExplicitThreshold(50);

        Assert.assertEquals(0,logger.logEntries.size());

        w.watchedMethodExplicitThreshold(150);

        Assert.assertEquals(1,logger.logEntries.size());
        Assert.assertTrue(logger.logEntries.get(0).startsWith("WARN logger: Long call=StopWatched#watchedMethodExplicitThreshold(150) executionTimeMS=1"));
    }

    @Test
    public void testNothingIsLoggedWhenFeatureDisabled() {
        StopWatched w = new StopWatched();

        w.watchedMethodExplicitThreshold(150);

        Assert.assertEquals(0,logger.logEntries.size());
    }

}
