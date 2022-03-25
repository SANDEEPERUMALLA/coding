package com.com.redis.perf;

public class Logger {

    public static void log(String message) {
        System.out.println(Thread.currentThread().getName() + " - " + message);
    }
}
