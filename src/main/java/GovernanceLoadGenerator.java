import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.net.UnknownHostException;

public class GovernanceLoadGenerator {

    public static void main(String[] args) throws UnknownHostException, InterruptedException {

//        HostAndPort hostAndPort = new HostAndPort("localhost", 6379);
//        JedisCluster jedisCluster = new JedisCluster(hostAndPort);
//        int index = 1;
//        for (int i = 1; i <= 2000; i++) {
//            for (int j = 1; j <= 10; j++) {
//                jedisCluster.set("ora1:caas::ORG" + j + ":key" + index, "value");
//                index++;
//            }
//        }

        RedisURI redisURI = RedisURI.Builder.redis("localhost",
                6379).build();
        RedisClient redisClient = RedisClient.create(redisURI);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        asyncCommands.unlink("key1").whenComplete((r, x) -> {
            System.out.println(Thread.currentThread().getName());
        });
        asyncCommands.get("key1").whenComplete((r, x) -> {
            System.out.println(Thread.currentThread().getName());
        });
    }

}
