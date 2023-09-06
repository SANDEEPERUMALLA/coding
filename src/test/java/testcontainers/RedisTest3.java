package testcontainers;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.protocol.ProtocolVersion;
import redis.embedded.Redis;
import redis.embedded.RedisCluster;
import redis.embedded.RedisServer;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

public class RedisTest3 {

    public static void main(String[] args) throws InterruptedException {
        RedisServer redisServer1 = new RedisServer(12000);
        RedisServer redisServer2 = new RedisServer(12001);
        RedisServer redisServer3 = new RedisServer(12002);
        redisServer1.start();
        redisServer2.start();
        redisServer3.start();

        InetSocketAddress node1 = new InetSocketAddress("localhost", 12000);
        InetSocketAddress node2 = new InetSocketAddress("localhost", 12001);
        InetSocketAddress node3 = new InetSocketAddress("localhost", 12003);

        RedisURI uri1 = RedisURI.create(node1.getAddress().getHostAddress(), 12000);
        RedisURI uri2 = RedisURI.create(node2.getAddress().getHostAddress(), 12001);
        RedisURI uri3 = RedisURI.create(node3.getAddress().getHostAddress(), 12002);

        ClusterClientOptions.Builder clusterClientOptionsBuilder = ClusterClientOptions.builder()
                .autoReconnect(true)
                .protocolVersion(ProtocolVersion.RESP3)
                .validateClusterNodeMembership(false)
                .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                        .enablePeriodicRefresh(Duration.ofSeconds(30))
                        .dynamicRefreshSources(true)
                        .enableAllAdaptiveRefreshTriggers()
                        .build());

        ClusterClientOptions clusterClientOptions = clusterClientOptionsBuilder.build();

        RedisClusterClient client1 = RedisClusterClient.create(uri1);
        client1.setOptions(clusterClientOptions);
        client1.refreshPartitions();

        StatefulRedisClusterConnection<String, String> connect = client1.connect();
        RedisAdvancedClusterCommands<String, String> commands = connect.sync();

        System.out.println();
        for (int i = 0; i <= 5000; i++) {
            commands.clusterAddSlots(i);
        }

        RedisClusterClient redisClusterClient2 = RedisClusterClient.create(uri2);
        redisClusterClient2.setOptions(clusterClientOptions);
        redisClusterClient2.refreshPartitions();
        redisClusterClient2.getPartitions();
        RedisAdvancedClusterCommands<String, String> commands2 = redisClusterClient2.connect().sync();
        for (int i = 5001; i <= 10000; i++) {
            commands2.clusterAddSlots(i);
        }

        RedisClusterClient redisClusterClient3 = RedisClusterClient.create(uri3);
        redisClusterClient3.setOptions(clusterClientOptions);
        redisClusterClient3.refreshPartitions();
        RedisAdvancedClusterCommands<String, String> commands3 = redisClusterClient3.connect().sync();

        for (int i = 10001; i <= 16383; i++) {
            commands3.clusterAddSlots(i);
        }

        System.out.println();


        String s = commands.clusterMeet("127.0.0.1", 12001);
        String s1 = commands.clusterMeet("127.0.0.1", 12002);

        RedisClusterClient redisClusterClient4 = RedisClusterClient.create(uri1);


        redisClusterClient4.setOptions(clusterClientOptions);
        redisClusterClient4.refreshPartitions();
        redisClusterClient4.getPartitions();
        RedisAdvancedClusterCommands<String, String> commands4 = redisClusterClient4.connect().sync();

        Thread.sleep(60000);

        commands.set("k1", "v1");
        commands.set("k2", "v2");
        commands.set("k3", "v3");
        System.out.println(commands.get("k1"));
        System.out.println(commands.get("k2"));
        System.out.println(commands.get("k3"));
    }
}
