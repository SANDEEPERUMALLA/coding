package testcontainers;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.*;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RedisTest {

    private Network network = Network.newNetwork();
    @Rule
    public RedisContainer redis1 = new RedisContainer(network, 12000);
    @Rule
    public RedisContainer redis2 = new RedisContainer(network, 12001);
    @Rule
    public RedisContainer redis3 = new RedisContainer(network, 12002);


    @Test
    public void test1() throws InterruptedException {
        System.out.println(redis1.getMappedPort(12000));
        System.out.println(redis2.getMappedPort(12001));
        System.out.println(redis3.getMappedPort(12002));

        ConcurrentMap<Integer, Integer> redisClusterNatPortMapping = new ConcurrentHashMap<>();


        redis1.setMappedPort(redis1.getMappedPort(12000));
        redis2.setMappedPort(redis2.getMappedPort(12001));
        redis3.setMappedPort(redis3.getMappedPort(12002));

        redisClusterNatPortMapping.put(12000, redis1.getMappedPort());
        redisClusterNatPortMapping.put(12001, redis2.getMappedPort());
        redisClusterNatPortMapping.put(12002, redis3.getMappedPort());

        InetSocketAddress node1 = new InetSocketAddress("localhost", redis1.getMappedPort());
        InetSocketAddress node2 = new InetSocketAddress("localhost", redis2.getMappedPort());
        InetSocketAddress node3 = new InetSocketAddress("localhost", redis3.getMappedPort());

        RedisURI uri1 = RedisURI.create(node1.getAddress().getHostAddress(), redis1.getMappedPort());
        RedisURI uri2 = RedisURI.create(node2.getAddress().getHostAddress(), redis2.getMappedPort());
        RedisURI uri3 = RedisURI.create(node3.getAddress().getHostAddress(), redis3.getMappedPort());

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


        String s = commands.clusterMeet(redis2.getAddress(), redis2.getPort());
        String s1 = commands.clusterMeet(redis3.getAddress(), redis3.getPort());

        ConcurrentMap<Integer, SocketAddress> redisClusterSocketAddresses = new ConcurrentHashMap<>();
        ClientResources clientResources = ClientResources.builder()
                .socketAddressResolver(new SocketAddressResolver(DnsResolver.unresolved()) {
                    @Override
                    public SocketAddress resolve(RedisURI redisURI) {
                        Integer mappedPort = redisClusterNatPortMapping.get(redisURI.getPort());
                        if (mappedPort != null) {
                            SocketAddress socketAddress = redisClusterSocketAddresses.get(mappedPort);
                            if (socketAddress != null) {
                                return socketAddress;
                            }
                            redisURI.setPort(mappedPort);
                        }

                        redisURI.setHost(DockerClientFactory.instance().dockerHostIpAddress());

                        SocketAddress socketAddress = super.resolve(redisURI);
                        redisClusterSocketAddresses.putIfAbsent(redisURI.getPort(), socketAddress);
                        return socketAddress;
                    }
                }).build();

        RedisClusterClient redisClusterClient4 = RedisClusterClient.create(clientResources, uri1);


        redisClusterClient4.setOptions(clusterClientOptions);
        redisClusterClient4.refreshPartitions();
        redisClusterClient4.getPartitions();
        RedisAdvancedClusterCommands<String, String> commands4 = redisClusterClient4.connect().sync();

        Thread.sleep(60000);

        commands4.set("k1", "v1");
        commands4.set("k2", "v2");
        commands4.set("k3", "v3");
        System.out.println(commands4.get("k1"));
        System.out.println(commands4.get("k2"));
        System.out.println(commands4.get("k3"));
    }
}
