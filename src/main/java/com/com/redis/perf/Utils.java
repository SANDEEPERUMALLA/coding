package com.com.redis.perf;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

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

    public static String generateRandomStringOfSize(int size) {
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder str = new StringBuilder();
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        for (int i = 1; i <= size; i++) {
            str.append(s.charAt(threadLocalRandom.nextInt(0, s.length() - 1)));
        }
        return str.toString();
    }

}
