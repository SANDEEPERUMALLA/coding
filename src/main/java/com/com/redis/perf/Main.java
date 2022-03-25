package com.com.redis.perf;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.com.redis.perf.Logger.log;
import static com.com.redis.perf.Utils.*;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Jedis jedis = new Jedis(URI.create("redis://localhost:6379"));
        setup(jedis);
        long start = System.currentTimeMillis();
        RedisUser user1 = new RedisUser(1);
        RedisUser user2 = new RedisUser(2);
        RedisUser user3 = new RedisUser(3);
        user1.start();
        user2.start();
        user3.start();
        Thread.sleep(60000);
        user1.stopThread();
        user2.stopThread();
        user3.stopThread();
        user1.join();
        user2.join();
        user3.join();
        List<RedisUser> users = List.of(user1, user2, user3);
        printRunStats(users);
        long end = System.currentTimeMillis();
        log("Total Run Time" + (end - start));
    }

    private static void printRunStats(List<RedisUser> users) {
        List<Long> latencies = new ArrayList<>();
        for(RedisUser user : users) {
            log(user.getName() + ":" + user.getNoOfOps());
            latencies.addAll(user.getLatencies());
        }
        printStats(latencies);
    }

    private static void setup(Jedis jedis) {
        log("Deleting set");
        jedis.del("set1");

        List<String> dates = getDates();
        List<String> values = new ArrayList<>();
        for (String date : dates) {
            for (int i = 1; i <= 10000; i++) {
                values.add(date + ":" + generateRandomStringOfSize(18));
            }
        }

        insertDataIntoRedisSortedSet(values, jedis);
    }


    private static void insertDataIntoRedisSortedSet(List<String> values, Jedis jedis) {
        log("Inserting data into set");
        Map<String, Double> redisSortedSetMap = new HashMap<>();
        int count = 0;
        int batchSize = 10_000;
        for (String value : values) {
            if (count == batchSize) {
                addValuesToSet(redisSortedSetMap, jedis);
                redisSortedSetMap.clear();
                count = 0;
            }
            redisSortedSetMap.put(value, 0d);
            count++;
        }
        addValuesToSet(redisSortedSetMap, jedis);
    }

    private static void addValuesToSet(Map<String, Double> redisSortedSetMap, Jedis jedis) {
        jedis.zadd("set1", redisSortedSetMap);
    }

    private static List<String> getDates() {
        LocalDateTime dateTime = LocalDateTime.now();
        List<String> dateStrings = new ArrayList<>();
        for (int i = 1; i <= 300; i++) {
            dateStrings.add(getDateString(dateTime));
            dateTime = dateTime.minusDays(1);
        }
        return dateStrings;
    }


}
