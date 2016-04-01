package com.github.jedis.lock;

import redis.clients.jedis.Jedis;

/**
 * Redis distributed lock implementation.
 */
public class JedisLock {

    Jedis jedis;

    /**
     * Lock key path.
     */
    String lockKey;

    /**
     * Lock expiration in miliseconds.
     * 锁超时，防止线程在入锁以后，无限的执行等待
     */
    int expireMsecs = 60 * 1000;

    /**
     * Acquire timeout in miliseconds.
     * 锁等待，防止线程饥饿
     */
    int timeoutMsecs = 10 * 1000;

    boolean locked = false;

    /**
     * Detailed constructor with default acquire timeout 10000 msecs and lock expiration of 60000 msecs.
     *
     * @param jedis
     * @param lockKey lock key (ex. account:1, ...)
     */
    public JedisLock(Jedis jedis, String lockKey) {
        this.jedis = jedis;
        this.lockKey = lockKey;
    }

    /**
     * Detailed constructor with default lock expiration of 60000 msecs.
     *
     * @param jedis
     * @param lockKey      lock key (ex. account:1, ...)
     * @param timeoutMsecs acquire timeout in miliseconds (default: 10000 msecs)
     */
    public JedisLock(Jedis jedis, String lockKey, int timeoutMsecs) {
        this(jedis, lockKey);
        this.timeoutMsecs = timeoutMsecs;
    }

    /**
     * Detailed constructor.
     *
     * @param jedis
     * @param lockKey      lock key (ex. account:1, ...)
     * @param timeoutMsecs acquire timeout in miliseconds (default: 10000 msecs)
     * @param expireMsecs  lock expiration in miliseconds (default: 60000 msecs)
     */
    public JedisLock(Jedis jedis, String lockKey, int timeoutMsecs, int expireMsecs) {
        this(jedis, lockKey, timeoutMsecs);
        this.expireMsecs = expireMsecs;
    }

    /**
     * Detailed constructor with default acquire timeout 10000 msecs and lock expiration of 60000 msecs.
     *
     * @param lockKey lock key (ex. account:1, ...)
     */
    public JedisLock(String lockKey) {
        this(null, lockKey);
    }

    /**
     * Detailed constructor with default lock expiration of 60000 msecs.
     *
     * @param lockKey      lock key (ex. account:1, ...)
     * @param timeoutMsecs acquire timeout in miliseconds (default: 10000 msecs)
     */
    public JedisLock(String lockKey, int timeoutMsecs) {
        this(null, lockKey, timeoutMsecs);
    }

    /**
     * Detailed constructor.
     *
     * @param lockKey      lock key (ex. account:1, ...)
     * @param timeoutMsecs acquire timeout in miliseconds (default: 10000 msecs)
     * @param expireMsecs  lock expiration in miliseconds (default: 60000 msecs)
     */
    public JedisLock(String lockKey, int timeoutMsecs, int expireMsecs) {
        this(null, lockKey, timeoutMsecs, expireMsecs);
    }

    /**
     * @return lock key
     */
    public String getLockKey() {
        return lockKey;
    }

    /**
     * Acquire lock.
     *
     * @return true if lock is acquired, false acquire timeouted
     * @throws InterruptedException in case of thread interruption
     */
    public synchronized boolean acquire() throws InterruptedException {
        return acquire(jedis);
    }

    /**
     * Acquire lock.
     *
     * @param jedis
     * @return true if lock is acquired, false acquire timeouted
     * @throws InterruptedException in case of thread interruption
     */
    public synchronized boolean acquire(Jedis jedis) throws InterruptedException {
        //锁等待，防止线程饥饿
        int timeout = timeoutMsecs;
        while (timeout >= 0) {
            //锁超时，防止线程在入锁以后，无限的执行等待(为什么加1:因为可能有相同时间的操作,这样做不完美,但是实用)
            long expires = System.currentTimeMillis() + expireMsecs + 1;
            String expiresStr = String.valueOf(expires); //锁到期时间
            if (jedis.setnx(lockKey, expiresStr) == 1) {
                // lock acquired
                locked = true;
                return true;
            }

            String currentValueStr = jedis.get(lockKey); //redis里的时间
            if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
                //判断是否为空，不为空的情况下，如果被其他线程设置了值，则第二个条件判断是过不去的
                // lock is expired

                String oldValueStr = jedis.getSet(lockKey, expiresStr);
                //获取上一个锁到期时间，并设置现在的锁到期时间，
                //只有一个线程才能获取上一个线上的设置时间，因为jedis.getSet是同步的
                if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                    //如过这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
                    // lock acquired
                    locked = true;
                    return true;
                }
            }
            timeout -= 100;
            Thread.sleep(100);
        }
        return false;
    }

    /**
     * Acqurired lock release.
     */
    public synchronized void release() {
        release(jedis);
    }

    /**
     * Acqurired lock release.
     */
    public synchronized void release(Jedis jedis) {
        if (locked) {
            jedis.del(lockKey);
            locked = false;
        }
    }
}
