package keyspacenotifications;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

public class RedisKeySpaceNotificationsTester {

    public static void main(String[] args) throws InterruptedException {
        RedisURI redisUri = RedisURI.Builder.redis("localhost", 6379).build();
        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connection = redisClient.connect();

        // Enable keyspace notifications
        connection.sync().configSet("notify-keyspace-events", "KEA");

        // Subscribe to 'set' events
        StatefulRedisPubSubConnection<String, String> pubSubConnection = redisClient.connectPubSub();
        RedisPubSubAdapter<String, String> pubSubAdapter = new RedisPubSubAdapter<String, String>() {

            @Override
            public void message(String pattern, String channel, String message) {
                // Process the received message
                System.out.println("Received message: " + message);
                System.out.println("Pattern: " +pattern);
                System.out.println("channel: " +channel);
            }

            @Override
            public void message(String channel, String message) {
                // Process the received message
                System.out.println("Received message: " + message);
            }
        };

        pubSubConnection.addListener(pubSubAdapter);
        pubSubConnection.sync().psubscribe("__keyevent@*__:expired");

        // Perform a set operation to trigger the event
        RedisCommands<String, String> commands = connection.sync();
        commands.set("mykey", "myvalue");

        // Wait for the event message
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cleanup and close connections
        pubSubConnection.sync().punsubscribe();
        pubSubConnection.removeListener(pubSubAdapter);
        pubSubConnection.close();
        connection.close();
        redisClient.shutdown();

    }
}
