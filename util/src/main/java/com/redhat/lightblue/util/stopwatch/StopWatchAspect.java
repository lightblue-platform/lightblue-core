package com.redhat.lightblue.util.stopwatch;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Aspect with pointcut for all methods annotated with @StopWatch. Measures execution time and logs a warning if it's higher than threshold.
 *
 * @author mpatercz
 *
 */
@Aspect
public class StopWatchAspect {

    static StopWatchLogger logger = new StopWatchLogger();

    @Around("@annotation(StopWatch) && execution(* *(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        if (!enabled()) {
            return joinPoint.proceed();
        }

        String loggerName = getAnnotation(joinPoint).loggerName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        final long start = System.nanoTime();

        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            throw t;
        } finally {
            final long tookNano = System.nanoTime() - start;
            final long tookMS = TimeUnit.MILLISECONDS.convert(tookNano, TimeUnit.NANOSECONDS);

            if (tookMS >= warnThresholdMS(joinPoint)) {
                logger.warn(loggerName, "Long call="+className+Mnemos.toText(joinPoint, false, false)+" executionTimeMS="+tookMS);
            }

            if (logger.isDebugEnabled(loggerName)) {
                logger.debug(loggerName, "call="+className+Mnemos.toText(joinPoint, false, false)+" executionTimeMS="+tookMS);
            }
        }


    }

    public static final String ENABLED_PROP = "stopwatch.enabled", GLOBAL_WARN_THRESHOLD_MS_PROP = "stopwatch.globalWarnThresholdMS";

    private StopWatch getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        return method.getAnnotation(StopWatch.class);
    }

    private static Boolean enabled = null;

    private boolean enabled() {
        if (enabled == null) {
            enabled = Boolean.parseBoolean(System.getProperty(ENABLED_PROP, "false"));
        }
        return enabled;
    }

    private static Integer globalWarnThresholdMS = null;

    private int globalWarnThresholdMS() {
        if (globalWarnThresholdMS == null) {
            globalWarnThresholdMS = Integer.parseInt(System.getProperty(GLOBAL_WARN_THRESHOLD_MS_PROP, "5000"));
        }
        return globalWarnThresholdMS;
    }

    private int warnThresholdMS(ProceedingJoinPoint joinPoint) {
        int annotationWarnThresholdMS = getAnnotation(joinPoint).warnThresholdMS();

        if (annotationWarnThresholdMS < 0) {
            return globalWarnThresholdMS();
        } else {
            return annotationWarnThresholdMS;
        }
    }

    // for testing only
    static void clear() {
        System.clearProperty(ENABLED_PROP);
        System.clearProperty(GLOBAL_WARN_THRESHOLD_MS_PROP);

        enabled = null;
        globalWarnThresholdMS = null;
    }



}
