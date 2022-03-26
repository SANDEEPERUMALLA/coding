package com.com.redis.perf;

public class Logger {

    private Logger() {}

    public static void log(String message) {
        System.out.println(System.currentTimeMillis() + " : " + Thread.currentThread().getName() + " - " + message);
    }
}
