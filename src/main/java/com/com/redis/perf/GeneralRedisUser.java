package com.com.redis.perf;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.com.redis.perf.Logger.log;

public class GeneralRedisUser implements RedisUser {

    private final Jedis jedis;
    private long noOfOps = 0;
    List<Long> latencies = new ArrayList<>();
    private final int userId;
    private static final String GENERAL_REDIS_USER_FORMAT = "redis-general-user";

    public GeneralRedisUser(int userId) {
        this.userId = userId;
        this.jedis = new Jedis(URI.create("redis://localhost:6379"));
    }

    @Override
    public void run() {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        while (!Thread.currentThread().isInterrupted()) {
            int keyIndex = threadLocalRandom.nextInt(0, 900);
            String key = "key" + keyIndex;
            long start = System.nanoTime();
            String value = jedis.get(key);
            long time = System.nanoTime() - start;
            noOfOps++;
            latencies.add(time);
            log("Get Op Time: " + time);
            log("Value length: " + value.length());
            log("Value: " + value.length());
        }
    }

    @Override
    public long getNoOfOps() {
        return noOfOps;
    }

    @Override
    public List<Long> getLatencies() {
        return latencies;
    }

    @Override
    public String getName() {
        return GENERAL_REDIS_USER_FORMAT + userId;
    }

    @Override
    public String getUserType() {
        return "GENERAL_USER";
    }
}
