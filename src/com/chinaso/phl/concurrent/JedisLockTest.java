package com.chinaso.phl.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.github.jedis.lock.JedisLock;

/**
 * @author come from network
 *         kk @date 2014-3-1
 */
public class JedisLockTest {
    private static ExecutorService executor = Executors.newFixedThreadPool(100);

    /**
     * @param args
     * @author piaohailin
     * @date 2014-3-1
     */
    public static void main(String[] args) {
//        final JedisPool pool = new JedisPool("192.168.56.2", 6379);
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        final JedisPool pool = new JedisPool(poolConfig, "192.168.142.237", 6379, 3000); //最后一个参数为密码
        final String key = "h";
        for (int i = 0; i < 5; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //模拟在分布式多台机子,所以用线程池来处理
                        Jedis jedis = pool.getResource();
                        JedisLock lock = new JedisLock(jedis, key, 3000, 3000);
                        if (lock.acquire()) {
                            //如果锁上了
                            try {
                                Thread.sleep(2000);
                                System.out.println(System.currentTimeMillis());
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                lock.release();//则解锁
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });

        }
        executor.shutdown();

    }

}