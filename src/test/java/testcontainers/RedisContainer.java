package testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.ArrayList;
import java.util.List;

public class RedisContainer extends GenericContainer<RedisContainer> {

    private static final String dockerImage = "ops0-artifactrepo1-0-prd.data.sfdc.net/docker-sfci-dev/sfci/cache-as-a-service/redis-docker:redisWithTls.6.2.6.6549ba7";
    private int mappedPort;
    private final int port;

    public int getMappedPort() {
        return mappedPort;
    }

//    @Override
//    public Integer getMappedPort(int originalPort) {
//        return originalPort;
//    }

    public void setMappedPort(int mappedPort) {
        this.mappedPort = mappedPort;
    }

    public RedisContainer(Network network, int port) {
        super(dockerImage);
        this.port = port;

        List<String> args = new ArrayList<>();
        args.add("--port=" + port);
        args.add("--noAuth");
        args.add("--disableProtectedMode");
        args.add("--enableCluster");
        this.withCommand(args.toArray(new String[0]));
        this.withNetwork(network);
        this.withExposedPorts(port);
    }

//    public String getAddress() {
//        return "127.0.0.1";
//    }
//
    public String getAddress() {
        return this.getContainerInfo()
                .getNetworkSettings()
                .getNetworks()
                .entrySet()
                .iterator()
                .next()
                .getValue()
                .getIpAddress();
    }

    @Override
    public String toString() {
        return getAddress() + ":" + mappedPort;
    }

    public int getPort() {
        return port;
    }

}
