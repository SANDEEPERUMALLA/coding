package redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashSet;
import java.util.Set;

public class LettuceClient {

    public void test() {
        System.out.println(this.getClass().getName());
        System.out.println(this.getClass().getSimpleName());
    }

    public static void main(String[] args) {
        LettuceClient lettuceClient = new LettuceClient();
        lettuceClient.test();

        //redis1();
//        RedisURI redisURI = RedisURI.builder()
//                .withHost("localhost")
//                .withAuthentication("default", "")
//                .withPort(6380)
//                .build();
//
//        System.out.println(redisURI);
//        RedisClient redisClient = RedisClient.create(redisURI);
//        StatefulRedisConnection<String, String> connection = redisClient.connect();
//        RedisCommands<String, String> syncCommands = connection.sync();
//        syncCommands.set("key", "value");
//        System.out.println(syncCommands.get("key"));
//        connection.close();
//        redisClient.shutdown();
    }

    public static void redis1() {

        RedisURI redisURI = RedisURI.builder()
                .withHost("localhost")
                .withPort(6379)
                .build();
        RedisClient redisClient = RedisClient.create(redisURI);
        long start = System.currentTimeMillis();
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {

            RedisCommands<String, String> syncCommands = connection.sync();
            Set<ScoredValue<String>> set = new HashSet<>();
            for (int i = 0; i <= 10_00_0000; i++) {
                set.add(ScoredValue.fromNullable(i, "test1234" + i));
            }
            syncCommands.zadd("set1", null, set.toArray(new ScoredValue[0]));
        } finally {
            redisClient.shutdown();
            System.out.println("Total Execution time in ms: " + (System.currentTimeMillis() - start));
        }


    }
}
