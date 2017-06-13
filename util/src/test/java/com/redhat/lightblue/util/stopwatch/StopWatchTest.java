package com.redhat.lightblue.util.stopwatch;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class StopWatchTest {

    static class StringSizeCalc implements SizeCalculator<String> {

        @Override
        public int size(String object) {
            return object.length();
        }

    }

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

        @StopWatch(loggerName="logger", sizeCalculatorClass="com.redhat.lightblue.util.stopwatch.StopWatchTest$StringSizeCalc", warnThresholdSizeB=3)
        public String explicitSize(String str) {
            return str;
        }

        @StopWatch(loggerName="logger", sizeCalculatorClass="com.redhat.lightblue.util.stopwatch.StopWatchTest$StringSizeCalc")
        public String implicitSize(String str) {
            return str;
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
        StopWatchAspect.stopWatchLogger = logger;
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
        Assert.assertTrue(logger.logEntries.get(0).startsWith("WARN logger: call=StopWatched#watchedMethodExplicitThreshold(150) executionTimeMS=1"));
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
        Assert.assertTrue(logger.logEntries.get(0).startsWith("WARN logger: call=StopWatched#watchedMethod(150) executionTimeMS=1"));
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
        Assert.assertTrue(logger.logEntries.get(0).startsWith("WARN logger: call=StopWatched#watchedMethodExplicitThreshold(150) executionTimeMS=1"));
    }

    @Test
    public void testNothingIsLoggedWhenFeatureDisabled() {
        StopWatched w = new StopWatched();

        w.watchedMethodExplicitThreshold(150);

        Assert.assertEquals(0,logger.logEntries.size());
    }

    @Test
    public void testResultSizeExplicitThreshold() {

        System.setProperty("stopwatch.enabled", "true");

        StopWatched w = new StopWatched();

        w.explicitSize("fo");

        Assert.assertEquals(0, logger.logEntries.size());

        w.explicitSize("foobar");

        Assert.assertEquals(1, logger.logEntries.size());
        Assert.assertEquals("WARN logger: call=StopWatched#explicitSize('foobar') resultSize=6", logger.logEntries.get(0));
    }

    @Test
    public void testResultSizeGlobalThreshold() {

        System.setProperty("stopwatch.enabled", "true");

        StopWatched w = new StopWatched();

        w.implicitSize("foobar");
        Assert.assertEquals(0, logger.logEntries.size());

        System.setProperty("stopwatch.stopwatch.globalWarnSizeThresholdB", "3");
        w.implicitSize("fo");
        Assert.assertEquals(0, logger.logEntries.size());

        w.explicitSize("foobar");

        Assert.assertEquals(1, logger.logEntries.size());
        Assert.assertEquals("WARN logger: call=StopWatched#explicitSize('foobar') resultSize=6", logger.logEntries.get(0));
    }

}
