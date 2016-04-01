package com;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

/**
 * Created by ycy on 16/3/31.
 */
public class tets {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("192.168.142.237");
        //set(String key, String value, String nxxx, String expx, long time) {
//        String value = System.currentTimeMillis() + "";
//      String status=  jedis.set("ycytest1", value, "NX", "PX", 60000l);
//        System.out.println(status);
//        jedis.setnx("ycytest1", "yiibai", "NX", "PX", 40000l);

        Integer i =new  Integer(null);
        System.out.println(i);
    }
}
