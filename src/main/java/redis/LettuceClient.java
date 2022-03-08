package redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.time.Duration;

public class LettuceClient {

    public static void main(String[] args) {

        redis1();
        System.out.println("hjdehfhe");
        System.out.println("hjdehfhe");
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

        System.out.println(redisURI);
        RedisClient redisClient = RedisClient.create(redisURI);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.set("key", "value", new SetArgs().ex(5));
        System.out.println(syncCommands.ttl("key"));;
        System.out.println(syncCommands.get("key"));
    }
}
