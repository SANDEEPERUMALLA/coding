package com.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Weigher;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CaffeineTester {

    public static void main(String[] args) throws InterruptedException {
        Cache<String, String> cache = Caffeine.newBuilder()
                .maximumWeight(300)
                .weigher(new Weigher<String, String>() {
                    @Override
                    public
                    @NonNegative int weigh(String key, String value) {
                        return 100;
                    }
                }).expireAfter(new Expiry<String, String>() {
                    @Override
                    public long expireAfterCreate(String key, String value,
                                                  long currentTime) {
                        return Duration.ofDays(200).toNanos();
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull String key, String value, long currentTime,
                                                  @NonNegative long currentDuration) {
                        return Duration.ofDays(200).toNanos();
                    }

                    @Override
                    public long expireAfterRead(@NonNull String key, String value, long currentTime,
                                                @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                }).build();

        ConcurrentMap<String, String> m1 = new ConcurrentHashMap<>();
        m1.put("k", "v");
        m1.put("k1", "v1");
        m1.put("k2", "v3");

        for (int i = 1; i <= 1000; i++) {
            long s1 = System.nanoTime();
            long e1 = System.nanoTime();
            System.out.println("Time: " + (e1 - s1));
        }

        ConcurrentMap<String, String> m = cache.asMap();
        m.put("k1", "v1");
        m.put("k2", "v2");
        m.put("k3", "v3");
        System.out.println(cache.asMap());
        m.put("k4", "v4");
        System.out.println(cache.asMap());
        //  Thread.sleep(10000);
        System.out.println(cache.asMap());
        m.put("k5", "v5");
        //   Thread.sleep(10000);
        System.out.println(cache.asMap());
        m.put("k6", "v6");
        // Thread.sleep(10000);
        for (int i = 1; i <= 100; i++) {
            long s = System.nanoTime();
            cache.getIfPresent("k4");
            long e = System.nanoTime();
            System.out.println("Time1: " + (e - s));
        }

        for (int i = 1; i <= 100; i++) {
            long s = System.nanoTime();
            cache.asMap().get("k4");
            long e = System.nanoTime();
            System.out.println("Time2: " + (e - s));
        }

        CacheStats stats = cache.stats();
        System.out.println(stats.hitRate());

        System.out.println(cache.asMap());
        for (int i = 1; i <= 10000; i++) {
            cache.getIfPresent("k4");
            cache.getIfPresent("k5");
            cache.getIfPresent("k6");
        }
        System.out.println(cache.asMap());
        Thread.sleep(10000);
        System.out.println(cache.asMap());

    }
}
