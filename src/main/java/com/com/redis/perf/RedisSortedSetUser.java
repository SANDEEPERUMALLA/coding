package com.com.redis.perf;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.com.redis.perf.Logger.log;
import static com.com.redis.perf.Utils.getDateString;

public class RedisSortedSetUser implements RedisUser {

    private final Jedis jedis;
    private long noOfOps = 0;
    List<Long> latencies = new ArrayList<>();
    private final int userId;
    private static final String REDIS_SORTED_USER_FORMAT = "redis-sorted-set-user";

    public RedisSortedSetUser(int userId) {
        this.userId = userId;
        this.jedis = new Jedis(URI.create("redis://localhost:6379"));
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getName());
        while (!Thread.currentThread().isInterrupted()) {
            LocalDateTime dateTime = LocalDateTime.now();
            for (int i = 1; i <= 200; i++) {
                String eR = getDateString(dateTime);
                dateTime = dateTime.minusDays(1);
                String sR = getDateString(dateTime);
                log("Range : [" + sR + " - " + eR + "]");
                long start = System.nanoTime();
//                Set<String> result = jedis.zrangeByLex("set1", "[" + sR, "[" + eR);
                long time = System.nanoTime() - start;
                noOfOps++;
                log("ZRange Op Time: " + time);
                latencies.add(time);
//                log("Result Size : " + result.size());
            }
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
        return REDIS_SORTED_USER_FORMAT + userId;
    }

    @Override
    public String getUserType() {
        return "SORTED_SET_USER";
    }
}
