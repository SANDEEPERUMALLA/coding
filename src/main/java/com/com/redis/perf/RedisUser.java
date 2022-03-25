package com.com.redis.perf;

import redis.clients.jedis.Jedis;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.com.redis.perf.Logger.log;
import static com.com.redis.perf.Utils.getDateString;

public class RedisUser extends Thread {

    private final Jedis jedis;

    private boolean stop = false;
    private long noOfOps = 0;

    public RedisUser(int userId) {
        super("redis-user" + userId);
        this.jedis = new Jedis(URI.create("redis://localhost:6379"));;
    }

    @Override
    public void run() {

        List<Long> times = new ArrayList<>();
        long s = System.currentTimeMillis();

        while (!stop) {
            LocalDateTime dateTime = LocalDateTime.now();
            for (int i = 1; i <= 200; i++) {
                long start = System.nanoTime();
                String eR = getDateString(dateTime);
                dateTime = dateTime.minusDays(1);
                String sR = getDateString(dateTime);
                log("Range : [" + sR + " - " + eR + "]");
                Set<String> result = jedis.zrangeByLex("set1", "[" + sR, "[" + eR);
                long time = System.nanoTime() - start;
                times.add(time);
                log("Result Size : " + result.size());
                noOfOps++;
            }
        }

        long e = System.currentTimeMillis();
        log("Total Read Time: " + (e - s));
        log("No Of Operations: " + noOfOps);
    }


    public void stopThread() {
        this.stop = true;
    }

    public long getNoOfOps() {
        return noOfOps;
    }
}
