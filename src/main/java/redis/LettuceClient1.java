package redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LettuceClient1 {

    public void test() {
        System.out.println(this.getClass().getName());
        System.out.println(this.getClass().getSimpleName());
    }

    public static void main(String[] args) throws Exception {
        redis1();
    }

    public static void redis1() throws Exception {

        RedisURI redisURI = RedisURI.builder()
                .socket("/tmp/redis.sock")
                .build();


        Runnable r1 = () -> {
            RedisClient redisClient = RedisClient.create(redisURI);
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            RedisCommands<String, String> syncCommands = connection.sync();
            for (int i = 1; i <= 100000000; i++) {
                syncCommands.set("k" + i, "v" + i);
                System.out.println(syncCommands.get("k" + i));
            }
        };

        Runnable r2 = () -> {
            RedisClient redisClient = RedisClient.create(redisURI);
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            RedisCommands<String, String> syncCommands = connection.sync();
            for (int i = 1; i <= 100000000; i++) {
                syncCommands.set("k" + i, "v" + i);
                System.out.println(syncCommands.get("k" + i));
            }
        };
        Runnable r3 = () -> {
            RedisClient redisClient = RedisClient.create(redisURI);
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            RedisCommands<String, String> syncCommands = connection.sync();
            for (int i = 1; i <= 100000000; i++) {
                syncCommands.set("k" + i, "v" + i);
                System.out.println(Thread.currentThread().getName() + syncCommands.get("k" + i));
            }
        };


        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.submit(r1);
        executorService.submit(r2);
        executorService.submit(r3);


    }
}
