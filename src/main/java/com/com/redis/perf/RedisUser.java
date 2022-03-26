package com.com.redis.perf;

import java.util.List;

public interface RedisUser extends Runnable {
    long getNoOfOps();

    List<Long> getLatencies();

    String getName();

    String getUserType();
}
