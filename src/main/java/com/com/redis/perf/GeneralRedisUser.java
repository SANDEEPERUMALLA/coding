package com.com.redis.perf;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.com.redis.perf.Logger.log;

public class GeneralRedisUser extends Thread implements RedisUser {

    private final Jedis jedis;
    private long noOfOps = 0;
    private boolean stop = false;
    List<Long> latencies = new ArrayList<>();

    public GeneralRedisUser(int userId) {
        super("redis-general-user" + userId);
        this.jedis = new Jedis(URI.create("redis://localhost:6379"));
    }

    @Override
    public void run() {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        while (!stop) {
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

    public void stopThread() {
        this.stop = true;
    }

    @Override
    public long getNoOfOps() {
        return noOfOps;
    }

    @Override
    public List<Long> getLatencies() {
        return latencies;
    }
}
