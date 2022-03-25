package com.redis.testing;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RangeScanTester {

    private static Jedis jedis;

    static {
        jedis = new Jedis(URI.create("redis://localhost:6379"));

    }

    public static void main(String[] args) {
        redisZRangeTest();
        testNavigationSet();
    }

    private static void redisZRangeTest() {
        print("-----------------------");
        print("REDIS OPS");
        jedis.del("set1");
        List<String> values = setupData();
        print("REDIS INSERT OP");
        insertDataIntoRedisSortedSet(values);
        print("REDIS READ OPS");
        readDataFromSortedSet();
        print("-----------------------");
    }

    private static void insertDataIntoRedisSortedSet(List<String> values) {
        Map<String, Double> redisSortedSetMap = new HashMap<>();
        int count = 0;
        int batchSize = 10_000;
        long start = System.nanoTime();
        for (String value : values) {
            if (count == batchSize) {
                addValuesToSet(jedis, redisSortedSetMap);
                redisSortedSetMap.clear();
                count = 0;
            }
            redisSortedSetMap.put(value, 0d);
            count++;
        }
        addValuesToSet(jedis, redisSortedSetMap);

        long end = System.nanoTime();
        printTime(end - start);
    }

    private static void readDataFromSortedSet() {
        LocalDateTime dateTime = LocalDateTime.now();
        List<Long> times = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            long start = System.nanoTime();
            String eR = sanitizeDay(dateTime.getDayOfMonth()) + "/" + dateTime.getMonth().getValue() + "/" + dateTime.getYear();
            dateTime = dateTime.minusDays(1);
            String sR = sanitizeDay(dateTime.getDayOfMonth()) + "/" + dateTime.getMonth().getValue() + "/" + dateTime.getYear();
            print("Range : [" + sR + " - " + eR + "]");
            Set<String> result = jedis.zrangeByLex("set1", "[" + sR, "[" + eR);
            long time = System.nanoTime() - start;
            printTime(time);
            times.add(time);
            print("Result Size : " + result.size());
        }

        IntSummaryStatistics statistics = times.stream().mapToInt(Long::intValue).summaryStatistics();
        print("Average Stats");
        printTime((long) statistics.getAverage());
    }

    private static void addValuesToSet(Jedis jedis, Map<String, Double> redisSortedSetMap) {
        jedis.zadd("set1", redisSortedSetMap);
    }

    private static void testNavigationSet() {
        print("-----------------------");
        print("NAVSET OPS");
        List<String> values = setupData();
        print("NAVSET INSERT OP");
        long start = System.nanoTime();
        NavigableSet<String> ns = new TreeSet<>(values);
        printTime(System.nanoTime() - start);
        print("Size : " + ns.size());

        print("NAVSET READ OPS");
        List<Long> times = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now();
        for (int i = 1; i <= 100; i++) {
            start = System.nanoTime();
            String eR = sanitizeDay(dateTime.getDayOfMonth()) + "/" + dateTime.getMonth().getValue() + "/" + dateTime.getYear();
            dateTime = dateTime.minusDays(1);
            String sR = sanitizeDay(dateTime.getDayOfMonth()) + "/" + dateTime.getMonth().getValue() + "/" + dateTime.getYear();
            print("Range : [" + sR + " - " + eR + "]");
            NavigableSet<String> result = new TreeSet<>();
            try {
                result = ns.subSet(sR, true, eR, true);
            } catch (Exception e) {
                // TODO
            }
            long time = System.nanoTime() - start;
            printTime(time);
            times.add(time);
            print("Result Size : " + result.size());
        }

        IntSummaryStatistics statistics = times.stream().mapToInt(Long::intValue).summaryStatistics();
        print("Average Stats");
        printTime((long) statistics.getAverage());

        print("-----------------------");

    }

    private static List<String> setupData() {
        List<String> dates = getDates();
        List<String> values = new ArrayList<>();
        for (String date : dates) {
            for (int i = 1; i <= 10000; i++) {
                values.add(date + ":" + generateRandomStringOfSize(18));
            }
        }
        return values;
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

    private static void printTime(long timeInNs) {
        print("Time: " + TimeUnit.NANOSECONDS.toMillis(timeInNs) + " ms," + TimeUnit.NANOSECONDS.toMicros(timeInNs) + "us");
    }

    private static List<String> getDates() {
        LocalDateTime dateTime = LocalDateTime.now();
        List<String> dateStrings = new ArrayList<>();
        for (int i = 1; i <= 300; i++) {
            dateStrings.add(sanitizeDay(dateTime.getDayOfMonth()) + "/" + dateTime.getMonth().getValue() + "/" + dateTime.getYear());
            dateTime = dateTime.minusDays(1);
        }
        return dateStrings;
    }

    private static String sanitizeDay(int d) {
        String day = String.valueOf(d);
        return day.length() == 1 ? "0" + day : day;
    }

    private static void print(String message) {
        System.out.println(message);
    }


}
