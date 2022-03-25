package com.redis.testing;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class RangeScanTester {

    private static final Jedis jedis;
    private static final int NO_OF_READ_OPS = 1000;

    static {
        jedis = new Jedis(URI.create("redis://localhost:6379"));
    }

    public static void main(String[] args) {
        testRedisZRange();
        testNavigationSet();
    }

    private static void testRedisZRange() {
        log("-----------------------");
        jedis.del("set1");
        log("REDIS OPS");
        List<String> values = setupData();
        log("REDIS INSERT OP");
        insertDataIntoRedisSortedSet(values);
        log("REDIS READ OPS");
        readDataFromSortedSet();
        log("-----------------------");
    }

    private static void insertDataIntoRedisSortedSet(List<String> values) {
        Map<String, Double> redisSortedSetMap = new HashMap<>();
        int count = 0;
        int batchSize = 10_000;
        long start = System.nanoTime();
        for (String value : values) {
            if (count == batchSize) {
                addValuesToSet(redisSortedSetMap);
                redisSortedSetMap.clear();
                count = 0;
            }
            redisSortedSetMap.put(value, 0d);
            count++;
        }
        addValuesToSet(redisSortedSetMap);

        long end = System.nanoTime();
        printTime(end - start);
    }

    private static void readDataFromSortedSet() {
        LocalDateTime dateTime = LocalDateTime.now();
        List<Long> times = new ArrayList<>();
        for (int i = 1; i <= NO_OF_READ_OPS; i++) {
            long start = System.nanoTime();
            String eR = getDateString(dateTime);
            dateTime = dateTime.minusDays(1);
            String sR = getDateString(dateTime);
            log("Range : [" + sR + " - " + eR + "]");
            Set<String> result = jedis.zrangeByLex("set1", "[" + sR, "[" + eR);
            long time = System.nanoTime() - start;
            printTime(time);
            times.add(time);
            log("Result Size : " + result.size());
        }

        IntSummaryStatistics statistics = times.stream().mapToInt(Long::intValue).summaryStatistics();
        log("Stats: ");
        log("No Of Operations: " + NO_OF_READ_OPS);
        printTime("Average Time:" , ((long) statistics.getAverage()));
    }

    private static void addValuesToSet(Map<String, Double> redisSortedSetMap) {
        jedis.zadd("set1", redisSortedSetMap);
    }

    private static void testNavigationSet() {
        log("-----------------------");
        log("NAVSET OPS");
        List<String> values = setupData();
        log("NAVSET INSERT OP");
        long start = System.nanoTime();
        NavigableSet<String> ns = new TreeSet<>(values);
        printTime(System.nanoTime() - start);
        log("Size : " + ns.size());

        log("NAVSET READ OPS");
        List<Long> times = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now();
        for (int i = 1; i <= NO_OF_READ_OPS; i++) {
            start = System.nanoTime();
            String eR = getDateString(dateTime);
            dateTime = dateTime.minusDays(1);
            String sR = getDateString(dateTime);
            log("Range : [" + sR + " - " + eR + "]");
            NavigableSet<String> result = new TreeSet<>();
            try {
                result = ns.subSet(sR, true, eR, true);
            } catch (Exception e) {
                // TODO
            }
            long time = System.nanoTime() - start;
            printTime(time);
            times.add(time);
            log("Result Size : " + result.size());
        }

        IntSummaryStatistics statistics = times.stream().mapToInt(Long::intValue).summaryStatistics();
        log("Stats: ");
        log("No Of Operations: " + NO_OF_READ_OPS);
        printTime("Average Time:" , ((long) statistics.getAverage()));

        log("-----------------------");
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

    private static void printTime(String timeIdentifier, long timeInNs) {
        log(timeIdentifier + ": " + TimeUnit.NANOSECONDS.toMillis(timeInNs) + " ms," + TimeUnit.NANOSECONDS.toMicros(timeInNs) + " us");
    }

    private static void printTime(long timeInNs) {
        printTime("Time: ", timeInNs);
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

    private static String sanitize(int d) {
        String day = String.valueOf(d);
        return day.length() == 1 ? "0" + day : day;
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static String getDateString(LocalDateTime localDateTime) {
        String month = sanitize(localDateTime.getMonth().getValue());
        String day = sanitize(localDateTime.getDayOfMonth());
        String year = String.valueOf(localDateTime.getYear());
        return month + "/" + day + "/" + year;
    }

}
