package com.redhat.lightblue.util.metrics;

import com.redhat.lightblue.util.metrics.RequestMetrics;

public class NoopRequestMetrics implements RequestMetrics {

    private static final NoopContext NOOP_CONTEXT = new NoopContext();

    @Override
    public Context startEntityRequest(String operation, String entity, String version) {
        return NOOP_CONTEXT;
    }

    @Override
    public Context startLockRequest(String lockOperation, String domain) {
        return NOOP_CONTEXT;
    }

    @Override
    public Context startBulkRequest(String bulkOperation) {
        return NOOP_CONTEXT;
    }

    private static class NoopContext implements Context {
        @Override
        public void endRequestMonitoring() {

        }

        @Override
        public void markRequestException(Exception e) {

        }

        @Override
        public void endRequestMonitoringWithException(Exception e) {
    
        }
    }
}
