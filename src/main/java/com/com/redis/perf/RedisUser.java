package com.com.redis.perf;

import java.util.List;

public interface RedisUser {
    long getNoOfOps();

    List<Long> getLatencies();

    String getName();
}
