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
       // redisZRangeTest();
        testNavigationSet();
    }

    private static void redisZRangeTest() {
        System.out.println("-----------------------");
        System.out.println("REDIS OPS");

        Jedis jedis = new Jedis(URI.create("redis://localhost:6379"));
        jedis.del("set1");
        List<String> values = setupData();
        System.out.println("REDIS INSERT OP");
        insertDataIntoRedisSortedSet(values);
        System.out.println("REDIS READ OPS");
        readDataFromSortedSet();
        System.out.println("-----------------------");
    }

    private static void insertDataIntoRedisSortedSet(List<String> values) {
        Map<String, Double> redisSortedSetMap = new HashMap<>();
        int count = 0;
        int batchSize = 10_000;
        long start = System.nanoTime();
        for (int i = 0; i < values.size(); i++) {
            if (count == batchSize) {
                addValuesToSet(jedis, redisSortedSetMap);
                redisSortedSetMap.clear();
                count = 0;
            }
            redisSortedSetMap.put(values.get(i), 0d);
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
            System.out.println("Range : [" + sR + " - " + eR + "]");
            Set<String> result = jedis.zrangeByLex("set1", "[" + sR, "[" + eR);
            long time = System.nanoTime() - start;
            printTime(time);
            times.add(time);
            System.out.println("Result Size : " + result.size());
        }

        IntSummaryStatistics statistics = times.stream().mapToInt(Long::intValue).summaryStatistics();
        System.out.println("Average Stats");
        printTime((long) statistics.getAverage());

    }

    private static void addValuesToSet(Jedis jedis, Map<String, Double> redisSortedSetMap) {
        jedis.zadd("set1", redisSortedSetMap);
    }

    private static void testNavigationSet() {
        System.out.println("-----------------------");
        System.out.println("NAVSET OPS");
        List<String> values = setupData();
        System.out.println("NAVSET INSERT OP");
        long start = System.nanoTime();
        NavigableSet<String> ns = new TreeSet<>(values);
        printTime(System.nanoTime() - start);
        System.out.println("Size : " + ns.size());

        System.out.println("NAVSET READ OPS");
        List<Long> times = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now();
        for (int i = 1; i <= 100; i++) {
            start = System.nanoTime();
            String eR = sanitizeDay(dateTime.getDayOfMonth()) + "/" + dateTime.getMonth().getValue() + "/" + dateTime.getYear();
            dateTime = dateTime.minusDays(1);
            String sR = sanitizeDay(dateTime.getDayOfMonth()) + "/" + dateTime.getMonth().getValue() + "/" + dateTime.getYear();
            System.out.println("Range : [" + sR + " - " + eR + "]");
            NavigableSet<String> result = new TreeSet<>();
            try {
                result = ns.subSet(sR, true, eR, true);
            } catch (Exception e) {

            }
            long time = System.nanoTime() - start;
            printTime(time);
            times.add(time);
            System.out.println(result);
            System.out.println("Result Size : " + result.size());
        }

        IntSummaryStatistics statistics = times.stream().mapToInt(Long::intValue).summaryStatistics();
        System.out.println("Average Stats");
        printTime((long) statistics.getAverage());

        System.out.println("-----------------------");

    }

    private static List<String> setupData() {
        List<String> dates = getDates();
        List<String> values = new ArrayList<>();
        for (String date : dates) {
            for (int i = 1; i <= 10; i++) {
                values.add(date + ":" + generateRandomStringOfSize(30));
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
        System.out.println("Time: " + TimeUnit.NANOSECONDS.toMillis(timeInNs) + " ms," + TimeUnit.NANOSECONDS.toMicros(timeInNs) + "us");
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


}
