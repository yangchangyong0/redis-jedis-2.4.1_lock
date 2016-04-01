/**
 * Copyright (c)  2011-2020 Panguso, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Panguso,
 * Inc. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.chinaso.phl.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.github.jedis.lock.JedisLock;

/**
 * 分布式锁的简单用法
 * @author come from network
 * @date 2014-3-7
 */
public class SimpleLock {
    private static Logger logger = LoggerFactory.getLogger(SimpleLock.class);

    private static JedisPool pool;
    private JedisLock jedisLock;
    private String lockKey;
    private Jedis jedis;
    private int timeoutMsecs;
    private int expireMsecs;

    public SimpleLock(String lockKey) {
        this(lockKey, 3000, 300000);
    }

    public SimpleLock(String lockKey, int timeoutMsecs, int expireMsecs) {
        this.lockKey = lockKey;
        this.jedis = pool.getResource();
        this.timeoutMsecs = timeoutMsecs;
        this.expireMsecs = expireMsecs;
        this.jedisLock = new JedisLock(jedis, lockKey.intern(), timeoutMsecs, expireMsecs);
    }

    public void wrap(Runnable runnable) {
        long begin = System.currentTimeMillis();
        try {
            // timeout超时，等待入锁的时间，设置为3秒；expiration过期，锁存在的时间设置为5分钟
            logger.info("begin logck,lockKey={},timeoutMsecs={},expireMsecs={}", lockKey, timeoutMsecs, expireMsecs);
            if (jedisLock.acquire()) { // 启用锁
                runnable.run();
            } else {
                //再入锁
                logger.info("The time wait for lock more than [{}] ms ", timeoutMsecs);
            }
        } catch (Throwable t) {
            // 分布式锁异常
            logger.warn(t.getMessage(), t);
        } finally {
            this.lockRelease(jedisLock, jedis);
        }
        logger.info("[{}]cost={}", lockKey, System.currentTimeMillis() - begin);
    }

    /**
     * 释放锁,后期欲将离线计算的释放锁封装
     *
     * @param lock
     * @param jedis
     * @author come from network
     * @date 2014-3-6
     */
    private void lockRelease(JedisLock lock,
                             Jedis jedis) {
        if (lock != null) {
            try {
                lock.release();// 则解锁
            } catch (Exception e) {
            }
        }
        if (jedis != null) {
            try {
                pool.returnResource(jedis);// 还到连接池里
            } catch (Exception e) {
            }
        }
        logger.info("release logck,lockKey={},timeoutMsecs={},expireMsecs={}", lockKey, timeoutMsecs, expireMsecs);
    }

    public static JedisPool getPool() {
        return pool;
    }

    public static synchronized void setPool(JedisPool pool) {
        SimpleLock.pool = pool;
    }

}
