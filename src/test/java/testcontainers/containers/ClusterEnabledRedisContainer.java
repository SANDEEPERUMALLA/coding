package testcontainers.containers;

import org.testcontainers.containers.Network;

import java.util.ArrayList;
import java.util.List;

public class ClusterEnabledRedisContainer extends RedisContainer {

    private static final String dockerImage = "ops0-artifactrepo1-0-prd.data.sfdc.net/docker-sfci-dev/sfci/cache-as-a-service/redis-docker:redisWithTls.6.2.6.6549ba7";

    public ClusterEnabledRedisContainer(Network network, int port) {
        super(network, port, dockerImage);
    }

    protected List<String> getArgs(int port) {
        List<String> args = super.getArgs(port);
        args.add("--enableCluster");
        return args;
    }
}
