package com.redis.testing;

import redis.clients.jedis.Jedis;

import java.net.URI;

public class RedisTester {

    public static void main(String[] args) {
        Jedis jedis = new Jedis(URI.create("redis://localhost:637"));
        jedis.set("key", "value123");
        System.out.println(jedis.get("key"));
    }
}
