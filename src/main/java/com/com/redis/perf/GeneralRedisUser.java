package com.com.redis.perf;

import redis.clients.jedis.Jedis;

import java.net.URI;

public class GeneralRedisUser extends Thread {

    private final Jedis jedis;

    public GeneralRedisUser(int userId) {
        super("redis-general-user" + userId);
        this.jedis = new Jedis(URI.create("redis://localhost:6379"));
    }

    @Override
    public void run() {

    }
}
