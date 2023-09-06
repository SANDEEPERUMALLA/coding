package com.navigableset.test;

import com.google.common.math.Stats;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.google.common.math.Quantiles.percentiles;
import static com.navigableset.test.logging.Logger.log;

public class LoadClient {

    private static int NO_OF_USERS = 10;
    private static int RUN_TIME_IN_SECS = 60;

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        Set<String> values = setupData();
        NavigableSet<String> navigableSet = new TreeSet<>(values);

        log("NS Insertion time: " + (System.currentTimeMillis() - start));
        log("Size : " + navigableSet.size());
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= NO_OF_USERS; i++) {
            users.add(new NavigableSetUser(navigableSet, i));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        start = System.currentTimeMillis();
        users.forEach(executorService::submit);

        Thread.sleep(RUN_TIME_IN_SECS * 1000L);
        executorService.shutdownNow();
        executorService.awaitTermination(2000, TimeUnit.SECONDS);
        long end = System.currentTimeMillis();
        printRunStats(users, start, end);
    }


    private static void printRunStats(List<User> users, long start, long end) {
        if (users.isEmpty()) {
            return;
        }

        long totalRunTime = TimeUnit.MILLISECONDS.toSeconds(end - start);
        log("Total Run Time in secs: " + totalRunTime);

        long totalOps = 0L;
        List<Long> latencies = new ArrayList<>();

        for (User user : users) {
            totalOps += user.getNoOfOps();
            log(user.getName() + ": " + user.getNoOfOps());
            latencies.addAll(user.getLatencies());
        }

        log("Total no of ops: " + totalOps);
        log("Throughout per sec: " + (totalOps / totalRunTime));
        printStats(latencies);
    }

    public static void printStats(List<Long> times) {
        double p99 = percentiles().index(99).compute(times);
        double p90 = percentiles().index(90).compute(times);
        double p75 = percentiles().index(50).compute(times);
        double p50 = percentiles().index(75).compute(times);
        double average = Stats.meanOf(times);
        log("Stats");
        printTime("P99", (long) p99);
        printTime("P90", (long) p90);
        printTime("P75", (long) p75);
        printTime("P50", (long) p50);
        printTime("Average", (long) average);
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

    private static Set<String> setupData() {

        Set<String> dates = getDateStringsForLastNDays(300);
        Set<String> values = new HashSet<>();
        for (String date : dates) {
            for (int i = 1; i <= 10_000; i++) {
                values.add(date + ":" + generateRandomStringOfSize(18));
            }
        }
        return values;
    }

    public static Set<String> getDateStringsForLastNDays(int n) {
        LocalDateTime dateTime = LocalDateTime.now();
        Set<String> dateStrings = new HashSet<>();
        for (int i = 1; i <= n; i++) {
            dateStrings.add(getDateString(dateTime));
            dateTime = dateTime.minusDays(1);
        }
        return dateStrings;
    }


    public static String getDateString(LocalDateTime localDateTime) {
        String month = sanitize(localDateTime.getMonth().getValue());
        String day = sanitize(localDateTime.getDayOfMonth());
        String year = String.valueOf(localDateTime.getYear());
        return month + "/" + day + "/" + year;
    }

    private static String sanitize(int d) {
        String day = String.valueOf(d);
        return day.length() == 1 ? "0" + day : day;
    }

    private static void printTime(String timeIdentifier, long timeInNs) {
        log(timeIdentifier + ": " + TimeUnit.NANOSECONDS.toMillis(timeInNs) + " ms," + TimeUnit.NANOSECONDS.toMicros(timeInNs) + " us"
                + "," + TimeUnit.NANOSECONDS.toNanos(timeInNs) + " ns");
    }

    private static void printTime(long timeInNs) {
        printTime("Time: ", timeInNs);
    }


}
