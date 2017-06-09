package com.redhat.lightblue.util.stopwatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use stopwatch to measure method execute time and log a warning if it's higher than threshold.
 *
 * @author mpatercz
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StopWatch {

    /**
     *  -1 means use global value
     */
    int warnThresholdMS() default -1;

    /**
     * slf4j logger name
     */
    String loggerName() default "com.redhat.lightblue.crud.stopwatch";

}
