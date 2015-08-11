package com.redhat.lightblue.extensions.synch;

public class InvalidLockException extends RuntimeException {
    public InvalidLockException(String resourceId) {
        super(resourceId);
    }

    public InvalidLockException() {
    }
}
