package com.com.redis.perf;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.com.redis.perf.Logger.log;
import static com.com.redis.perf.Utils.*;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Jedis jedis = new Jedis(URI.create("redis://localhost:6379"));
        setup(jedis);
        long start = System.currentTimeMillis();

        int noOfUsers = 3;
        List<RedisSortedSetUser> users = new ArrayList<>();
        for (int i = 1; i <= noOfUsers; i++) {
            RedisSortedSetUser redisSortedSetUser = new RedisSortedSetUser(i);
            users.add(redisSortedSetUser);
        }

        for(RedisSortedSetUser user : users) {
            user.start();
        }

        Thread.sleep(60000);

        for(RedisSortedSetUser user : users) {
            user.stopThread();
        }

        for(RedisSortedSetUser user : users) {
            user.join();
        }

        long end = System.currentTimeMillis();
        printRunStats(users, start, end);
    }

    private static void printRunStats(List<RedisSortedSetUser> users, long start, long end) {
        long totalRunTime = TimeUnit.MILLISECONDS.toMillis(end - start);
        log("Total Run Time in ms: " + totalRunTime);

        long totalOps = 0L;
        List<Long> latencies = new ArrayList<>();

        for (RedisSortedSetUser user : users) {
            totalOps += user.getNoOfOps();
            log(user.getName() + ": " + user.getNoOfOps());
            latencies.addAll(user.getLatencies());
        }

        log("Total no of ops: " + totalOps);
        log("Throughout per sec: " + (totalOps / totalRunTime));
        printStats(latencies);
    }

    private static void setup(Jedis jedis) {
        setupSetData(jedis);
    }

    private static void setupKVData() {

    }

    private static void setupSetData(Jedis jedis) {
        log("Deleting set");
        jedis.del("set1");

        List<String> dates = getDateStringsForLastNDays(300);
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

}
