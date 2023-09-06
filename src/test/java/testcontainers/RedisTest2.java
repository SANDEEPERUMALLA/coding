package testcontainers;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.protocol.ProtocolVersion;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.Network;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;

public class RedisTest2 {


    @Test
    public void test1() throws InterruptedException {
//        System.out.println(redis1.getMappedPort(12000));
//        System.out.println(redis2.getMappedPort(12001));
//        System.out.println(redis3.getMappedPort(12002));
//
//        redis1.setMappedPort(redis1.getMappedPort(12000));
//        redis2.setMappedPort(redis2.getMappedPort(12001));
//        redis3.setMappedPort(redis3.getMappedPort(12002));

        InetSocketAddress node1 = new InetSocketAddress("localhost", 12000);
        InetSocketAddress node2 = new InetSocketAddress("localhost", 12001);
        InetSocketAddress node3 = new InetSocketAddress("localhost", 12002);

        RedisURI uri1 = RedisURI.create(node1.getAddress().getHostAddress(), 12000);
        RedisURI uri2 = RedisURI.create(node2.getAddress().getHostAddress(), 12001);
        RedisURI uri3 = RedisURI.create(node3.getAddress().getHostAddress(), 12002);

        ClusterClientOptions.Builder clusterClientOptionsBuilder = ClusterClientOptions.builder()
                .autoReconnect(true)
                .protocolVersion(ProtocolVersion.RESP3)
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

        String s = commands.clusterMeet(node2.getAddress().getHostAddress(), 12001);
        String s1 = commands.clusterMeet(node3.getAddress().getHostAddress(), 12002);

        System.out.println("test");

        Thread.sleep(60000);
        redisClusterClient3.refreshPartitions();
        redisClusterClient3.getPartitions();
        commands3.set("k1", "v1");
        commands3.set("k2", "v2");
        commands3.set("k3", "v3");
        System.out.println(commands3.get("k1"));
        System.out.println(commands3.get("k2"));
        System.out.println(commands3.get("k3"));
    }
}
