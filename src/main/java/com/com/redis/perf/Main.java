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

        int noOfSortedSetUsers = 1;
        List<RedisSortedSetUser> sortedSetUsers = new ArrayList<>();
        for (int i = 1; i <= noOfSortedSetUsers; i++) {
            RedisSortedSetUser redisSortedSetUser = new RedisSortedSetUser(i);
            sortedSetUsers.add(redisSortedSetUser);
        }

        int nofGeneralUsers = 3;
        List<GeneralRedisUser> generalRedisUsers = new ArrayList<>();
        for (int i = 1; i <= nofGeneralUsers; i++) {
            GeneralRedisUser generalRedisUser = new GeneralRedisUser(i);
            generalRedisUsers.add(generalRedisUser);
        }

        for (RedisSortedSetUser user : sortedSetUsers) {
            user.start();
        }

        for (GeneralRedisUser user : generalRedisUsers) {
            user.start();
        }

        Thread.sleep(60000);

        for (RedisSortedSetUser user : sortedSetUsers) {
            user.stopThread();
        }

        for (GeneralRedisUser user : generalRedisUsers) {
            user.stopThread();
        }

        for (RedisSortedSetUser user : sortedSetUsers) {
            user.join();
        }

        for (GeneralRedisUser user : generalRedisUsers) {
            user.join();
        }

        long end = System.currentTimeMillis();
        printRunStats(sortedSetUsers, start, end);
        printRunStatsGeneral(generalRedisUsers, start, end);
    }

    private static void printRunStats(List<RedisSortedSetUser> users, long start, long end) {
        long totalRunTime = TimeUnit.MILLISECONDS.toMillis(end - start);
        log("Total Run Time in ms: " + totalRunTime);

        long totalOps = 0L;
        List<Long> latencies = new ArrayList<>();

        for (RedisUser user : users) {
            totalOps += user.getNoOfOps();
            log(user.getName() + ": " + user.getNoOfOps());
            latencies.addAll(user.getLatencies());
        }

        log("Total no of ops: " + totalOps);
        log("Throughout per sec: " + (totalOps / totalRunTime));
        printStats(latencies);
    }

    private static void printRunStatsGeneral(List<GeneralRedisUser> users, long start, long end) {
        long totalRunTime = TimeUnit.MILLISECONDS.toMillis(end - start);
        log("Total Run Time in ms: " + totalRunTime);

        long totalOps = 0L;
        List<Long> latencies = new ArrayList<>();

        for (GeneralRedisUser user : users) {
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
        setupKVData(jedis);
    }

    private static void setupKVData(Jedis jedis) {

        for (int i = 0; i <= 10000; i++) {
            String key = "key" + i;
            String value = generateRandomStringOfSize(10000);
            jedis.set(key, value);
        }
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
