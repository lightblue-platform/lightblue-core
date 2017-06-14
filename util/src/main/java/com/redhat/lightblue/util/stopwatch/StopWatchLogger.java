package com.redhat.lightblue.util.stopwatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A logging wrapper to make unit tests easier.
 *
 * @author mpatercz
 *
 */
public class StopWatchLogger {

    private Logger getLogger(String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }

    public void warn(String loggerName, String message) {
        getLogger(loggerName).warn(message);
    }

    public void debug(String loggerName, String message) {
        getLogger(loggerName).debug(message);
    }

    public boolean isDebugEnabled(String loggerName) {
        return getLogger(loggerName).isDebugEnabled();
    }

}
