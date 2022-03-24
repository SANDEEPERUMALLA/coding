package com.redis.testing;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RedisTester {

    public static void main(String[] args) {
        Jedis jedis = new Jedis(URI.create("redis://localhost:6379"));
        Map<String, Double> redisSortedSetMap = new HashMap<>();
        for (int i = 0; i <= 1000000; i++) {
            redisSortedSetMap.put(String.valueOf("test123456" + i), 0d);
        }

        long start = System.currentTimeMillis();
        jedis.zadd("set1", redisSortedSetMap);
        System.out.println("Total Execution time : " + (System.currentTimeMillis() - start));


    }
}
