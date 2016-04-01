package com;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author piaohailin
 * @date 2014-3-1
 */
public class JedisTest {
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
                        Jedis jedis = pool.getResource();
                        String valueNow = jedis.get("ycytest1");
                        if (valueNow != null) {
                            //如果锁上了
                            Thread.sleep(2000);
                            System.out.println(System.currentTimeMillis());

                        } else {
                            //没有锁定的情况
                            String value = (System.currentTimeMillis() + 1) + "";
                            String status = jedis.set("ycytest1", value, "NX", "PX", 4000l);

                            if (status != null) {
                                Thread.sleep(2000);
                                System.out.println("s" + System.currentTimeMillis());
                            }
                            System.out.println(1);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
        }
    }

}