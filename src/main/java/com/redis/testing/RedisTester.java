package com.redis.testing;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RedisTester {

    public static void main(String[] args) {
        insertDataIntoRedisSortedSet();
        // readDataFromSortedSet();
    }

//    private static void readDataFromSortedSet() {
//        Jedis jedis = new Jedis(URI.create("redis://localhost:6379"));
//        long start = System.currentTimeMillis();
////        Set<String> result = jedis.zrangeByLex("set1", "[1000", "[1010");
////        System.out.println("Result Size: " + result.size());
//        //System.out.println(result);
//        long end = System.currentTimeMillis();
//        System.out.println("Read Total Time: " + (end - start));
//    }

    private static void insertDataIntoRedisSortedSet() {

        Jedis jedis = new Jedis(URI.create("redis://localhost:6379"));
        Map<String, Double> redisSortedSetMap = new HashMap<>();
        int count = 0;
        int totalEntries = 3_000_000;
        int batchSize = 10_000;
        long start = System.currentTimeMillis();
        for (int i = 1; i <= totalEntries; i++) {
            if (count == batchSize) {
                addValuesToSet(jedis, redisSortedSetMap);
                redisSortedSetMap.clear();
                count = 0;
            }
            redisSortedSetMap.put(i + "testingstringqwerty12345678910", 0d);
            count++;
        }
        addValuesToSet(jedis, redisSortedSetMap);

        long end = System.currentTimeMillis();
        System.out.println("Inset Total Time: " + (end - start));
    }

    private static void addValuesToSet(Jedis jedis, Map<String, Double> redisSortedSetMap) {
        jedis.zadd("set1", redisSortedSetMap);
    }

    private static String generateRandomStringOfSize(int size) {
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder str = new StringBuilder();
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        for (int i = 1; i <= size; i++) {
            str.append(s.charAt(threadLocalRandom.nextInt(0, s.length() - 1)));
        }
        return str.toString();
    }

}
