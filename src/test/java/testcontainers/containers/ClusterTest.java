package testcontainers.containers;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.resource.ClientResources;
import org.junit.Rule;
import org.junit.Test;

import java.net.InetSocketAddress;

public class ClusterTest {


    @Rule
    public final RedisClusterStartupRules redisClusterStartupRules  = new RedisClusterStartupRules();

    @Test
    public void test() {
        InetSocketAddress clusterAddress = redisClusterStartupRules.getClusterAddress();
        RedisURI redisURI = RedisURI.create(clusterAddress.getAddress().getHostAddress(), clusterAddress.getPort());
        ClientResources clientResources = ClientResources.builder().socketAddressResolver(RedisClusterStartupRules.getSocketAddressResolver()).build();
        RedisClusterClient clusterClient = RedisClusterClient.create(clientResources, redisURI);
        clusterClient.setOptions(RedisClusterStartupRules.getClusterClientOptions());
        clusterClient.refreshPartitions();
        clusterClient.getPartitions();

        StatefulRedisClusterConnection<String, String> connection = clusterClient.connect();
        RedisAdvancedClusterCommands<String, String> clusterCommands = connection.sync();

        clusterCommands.set("k1", "v1");
        System.out.println(clusterCommands.get("k1"));
    }


}
