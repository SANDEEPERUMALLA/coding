package com.com.redis.perf;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.com.redis.perf.Logger.log;
import static com.com.redis.perf.Utils.*;

public class Main {

    private final static int RUN_TIME_ON_SECS = 60;

    public static void main(String[] args) throws InterruptedException {
        Jedis jedis = new Jedis(URI.create("redis://localhost:6379"));
        setup(jedis);
        long start = System.currentTimeMillis();

        int noOfSortedSetUsers = 1;
        List<RedisUser> sortedSetUsers = new ArrayList<>();
        for (int i = 1; i <= noOfSortedSetUsers; i++) {
            RedisUser redisSortedSetUser = new RedisSortedSetUser(i);
            sortedSetUsers.add(redisSortedSetUser);
        }

        int nofGeneralUsers = 1;
        List<RedisUser> generalRedisUsers = new ArrayList<>();
        for (int i = 1; i <= nofGeneralUsers; i++) {
            RedisUser generalRedisUser = new GeneralRedisUser(i);
            generalRedisUsers.add(generalRedisUser);
        }

        List<Runnable> tasks = new ArrayList<>();
        tasks.addAll(sortedSetUsers);
        tasks.addAll(generalRedisUsers);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        tasks.forEach(executorService::submit);

        Thread.sleep(RUN_TIME_ON_SECS * 1000);
        executorService.shutdownNow();
        executorService.awaitTermination(2000, TimeUnit.SECONDS);

        long end = System.currentTimeMillis();
        printRunStats(sortedSetUsers, start, end);
        printRunStats(generalRedisUsers, start, end);
    }

    private static void printRunStats(List<RedisUser> users, long start, long end) {
        log("Stats for: " + users.get(0).getUserType());

        long totalRunTime = TimeUnit.MILLISECONDS.toSeconds(end - start);
        log("Total Run Time in secs: " + totalRunTime);

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

    private static void setup(Jedis jedis) {
        setupSetData(jedis);
        setupKVData(jedis);
    }

    private static void setupKVData(Jedis jedis) {
        for (int i = 0; i <= 10_000; i++) {
            String key = "key" + i;
            String value = generateRandomStringOfSize(50_000);
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
