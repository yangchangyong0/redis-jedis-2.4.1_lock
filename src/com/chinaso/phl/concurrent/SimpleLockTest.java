package com.chinaso.phl.concurrent;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author com from network
 * @date 2014-3-13
 */
public class SimpleLockTest {
    private static ExecutorService executor = Executors.newFixedThreadPool(100);

    /**
     * @param args
     * @author com from network
     * @date 2014-3-13
     */
    public static void main(String[] args) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        final JedisPool pool = new JedisPool(poolConfig, "192.168.142.237", 6379, 3000); //最后一个参数为密码
        SimpleLock.setPool(pool);//只需要初始化一次


        for (int i = 0; i < 5; i++) {
            executor.execute(new Runnable() {
                //模拟多线程
                String key = "test";
                SimpleLock lock = new SimpleLock(key, 3000, 300000);
                public void run() {

                    lock.wrap(new Runnable() {
                        public void run() {
                            //此处代码是锁上的
                            try {
                                Thread.sleep(4000);
                                System.out.println(System.currentTimeMillis());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

        }
        executor.shutdown();

    }

}
