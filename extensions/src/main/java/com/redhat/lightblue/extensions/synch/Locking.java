package com.redhat.lightblue.extensions.synch;

/**
 * Implements reentrant lock semantics on logical resources.
 */
public interface Locking {

    /**
     * Acquire a lock on the resource in the given domain for the caller
     *
     * @param domain A string identifying the locking domain. 
     * @param callerId A string identifying the calling process
     * @param resourceId A string identifying the resource being locked
     * @param ttl Optional expiration time for the lock
     *
     * The <code>domain</code> specifies the implementation dependent
     * datastore that keeps the lock information.
     *
     * The <code>callerId</code> uniquely identifies the caller.
     * 
     * The <code>resourceId</code> identifies a resource.
     * 
     * The optional <code>ttl</code> gives the time-to-live for the
     * lock in milliseconds. The lock expires after <code>ttl</code>
     * milliseconds pass and no activity is recorded on the lock.
     *
     * The implementation atomically locks the given resource. That
     * is, if many callers submit acquire requests with the same
     * domain and resourceId but with different callerIds, only one of
     * them will return success, and all others will fail. Subsequent
     * calls to acquire by different callers will fail until the lock
     * owner releases the lock. If the lock owner calls acquire,
     * matching number of release calls must be submitted to release
     * the lock.
     *
     * @return <code>true</code> if the resource is acquired,
     * <code>false</code> if the acquisition failed.
     */
    boolean acquire(String domain,String callerId,String resourceId,Long ttl);

    /**
     * Releases a lock on a resource
     *
     * @param domain A string identifying the locking domain. 
     * @param callerId A string identifying the calling process
     * @param resourceId A string identifying the locked resource
     *
     * Releases a lock identified by the <code>resourceId</code>. The
     * lock must be acquired by the <code>callerId</code> before in
     * the same <code>domain</code>. A release call decreases the
     * acquire count of the lock, and when that count reaches zero,
     * the resource is no longer locked.
     *
     * @return <code>true</code> if the lock count reaches zero and
     * the lock is released, <code>false</code> if the lock is not
     * released by this call because lock count is greated than zero.
     * 
     * @throws InvalidLockException If lock is not owned by the caller
     */
    boolean release(String domain,String callerId,String resourceId);

    /**
     * Checks if a resource lock is held by the caller
     *
     * @param domain A string identifying the locking domain. 
     * @param callerId A string identifying the calling process
     * @param resourceId A string identifying the locked resource
     *
     * Returns the number of the lock count if the
     * <code>callerId</code> owns the lock in the <code>domain</code>
     * for the resource <code>resourceId</code>. 
     *
     * @throws InvalidLockException If lock is not owned by the caller
     */
    int getLockCount(String domain,String callerId,String resourceId);

    /**
     * Extends the lock lifetime
     *
     * @param domain A string identifying the locking domain. 
     * @param callerId A string identifying the calling process
     * @param resourceId A string identifying the locked resource
     * 
     * Restarts the time-to-live counter to notify that the process
     * holding the lock is still alive.
     * 
     * @throws InvalidLockException If lock is not owned by the caller
     */
    void ping(String domain,String callerId,String resourceId);
}
