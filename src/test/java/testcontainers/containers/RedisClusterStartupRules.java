package testcontainers.containers;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.SocketAddressResolver;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.lettuce.core.cluster.SlotHash.SLOT_COUNT;

public class RedisClusterStartupRules implements TestRule {

    private final int PORT = 12000;
    private final Network network = Network.newNetwork();
    private final List<ClusterEnabledRedisContainer> clusterEnabledRedisContainers = new ArrayList<>();
    private final int clusterSize;
    private static final  int DEFAULT_CLUSTER_SIZE = 3;
    private static SocketAddressResolver socketAddressResolver;
    private final InetSocketAddress[] socketAddresses;

    public RedisClusterStartupRules() {
        this(DEFAULT_CLUSTER_SIZE);
    }

    public RedisClusterStartupRules(int clusterSize) {
        this.clusterSize = clusterSize;
        socketAddresses = new InetSocketAddress[clusterSize];
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    startup();
                    base.evaluate();
                } finally {
                    shutdown();
                }
            }
        };
    }

    private void shutdown() {
        clusterEnabledRedisContainers.forEach(GenericContainer::stop);
        network.close();
    }

    private void startup() {
        int port = PORT;
        for (int i = 1; i <= clusterSize; i++) {
            ClusterEnabledRedisContainer clusterEnabledRedisContainer = new ClusterEnabledRedisContainer(network, port++);
            clusterEnabledRedisContainer.start();
            clusterEnabledRedisContainers.add(clusterEnabledRedisContainer);
        }
        setupCluster();
    }

    private void setupCluster() {

        Map<Integer, Integer> redisClusterNatPortMapping = new ConcurrentHashMap<>();

        RedisURI[] redisURIS = new RedisURI[clusterSize];
        for (int i = 0; i < clusterSize; i++) {
            Integer mappedPort = clusterEnabledRedisContainers.get(i).getMappedPort(PORT + i);
            socketAddresses[i] = new InetSocketAddress("localhost", mappedPort);
            redisURIS[i] = RedisURI.create(socketAddresses[i].getAddress().getHostAddress(), socketAddresses[i].getPort());
            redisClusterNatPortMapping.put(PORT + i, mappedPort);
        }

        ClusterClientOptions clusterClientOptions = getClusterClientOptions();
        int noOfSlotsPerRedis = SLOT_COUNT/clusterSize;

        // Distribute slots across the redis servers in the cluster
        int slotStart = 0;
        for (int i = 0; i < clusterSize; i++) {
            RedisClusterClient clusterClient = RedisClusterClient.create(redisURIS[i]);
            clusterClient.setOptions(clusterClientOptions);
            clusterClient.refreshPartitions();
            clusterClient.getPartitions();

            StatefulRedisClusterConnection<String, String> connection = clusterClient.connect();
            RedisAdvancedClusterCommands<String, String> clusterCommands = connection.sync();

            int slotEnd = slotStart + noOfSlotsPerRedis;
            if (i == clusterSize - 1) {
                slotEnd = SLOT_COUNT;
            }
            for (int j = slotStart; j < slotEnd; j++) {
                clusterCommands.clusterAddSlots(j);
            }
            slotStart = slotEnd;
        }

        socketAddressResolver = new CustomSocketAddressResolver(redisClusterNatPortMapping);
        ClientResources clientResources = ClientResources.builder().socketAddressResolver(socketAddressResolver).build();
        RedisClusterClient redisClusterClient = RedisClusterClient.create(clientResources, redisURIS[0]);
        redisClusterClient.setOptions(clusterClientOptions);
        redisClusterClient.refreshPartitions();
        redisClusterClient.getPartitions();
        RedisAdvancedClusterCommands<String, String> commands = redisClusterClient.connect().sync();

        // Perform cluster meet
        for (int i = 1; i < clusterSize; i++) {
            ClusterEnabledRedisContainer redisContainer = clusterEnabledRedisContainers.get(i);
            commands.clusterMeet(redisContainer.getAddress(), redisContainer.getPort());
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        redisClusterClient = RedisClusterClient.create(clientResources, redisURIS[0]);
        redisClusterClient.setOptions(clusterClientOptions);
        redisClusterClient.refreshPartitions();
        redisClusterClient.getPartitions();
        RedisAdvancedClusterCommands<String, String> commands3 = redisClusterClient.connect().sync();

        // Perform gets and puts to validate the formed cluster
        commands3.set("k1", "v1");
        commands3.set("k2", "v2");
        commands3.set("k3", "v3");
        System.out.println(commands3.get("k1"));
        System.out.println(commands3.get("k2"));
        System.out.println(commands3.get("k3"));
        String clusterInfo = commands3.clusterInfo();

        if(!clusterInfo.contains("cluster_state:ok")) {
            throw new RuntimeException("Cluster could not be formed");
        }
        commands3.set("k1", "v1");
        commands3.set("k2", "v2");
        commands3.set("k3", "v3");
        System.out.println(commands3.get("k1"));
        System.out.println(commands3.get("k2"));
        System.out.println(commands3.get("k3"));
        System.out.println(clusterInfo);
    }

    public static ClusterClientOptions getClusterClientOptions() {
        ClusterClientOptions.Builder clusterClientOptionsBuilder = ClusterClientOptions.builder()
                .autoReconnect(true)
                .protocolVersion(ProtocolVersion.RESP3)
                .validateClusterNodeMembership(false)
                .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                        .enablePeriodicRefresh(Duration.ofSeconds(30))
                        .dynamicRefreshSources(true)
                        .enableAllAdaptiveRefreshTriggers()
                        .build());

        return clusterClientOptionsBuilder.build();
    }


    public static SocketAddressResolver getSocketAddressResolver() {
        return socketAddressResolver;
    }

    public InetSocketAddress getClusterAddress() {
        return socketAddresses[0];
    }
}
