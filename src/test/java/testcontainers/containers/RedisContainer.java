package testcontainers.containers;

import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

public class RedisContainer extends GenericContainer<RedisContainer> {
    private static final String dockerImageName = "ops0-artifactrepo1-0-prd.data.sfdc.net/docker-sfci-dev/sfci/cache-as-a-service/redis-docker:6.2.5.f6c2222";
    private final int port;

    public RedisContainer(Network network, int port) {
        this(network, port, dockerImageName);
    }

    public RedisContainer(Network network, int port, String dockerImageName) {
        super(dockerImageName);
        this.port = port;
        this.withExposedPorts(port);
        List<String> args = getArgs(port);
        this.withNetwork(network);
        this.withCommand(args.toArray(new String[0]));
    }

    protected List<String> getArgs(int port) {
        List<String> args = new ArrayList<>();
        args.add("--port=" + port);
        args.add("--noAuth");
        args.add("--disableProtectedMode");
        return args;
    }

    public int getPort() {
        return this.port;
    }
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
}