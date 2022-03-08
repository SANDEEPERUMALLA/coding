package redis;

import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

public class RedisContainer extends GenericContainer<RedisContainer> {
    private static final String dockerImageName = "registry.hub.docker.com/library/redis:6.2.6";

    public RedisContainer(int port) {
        super(dockerImageName);
        Network network = Network.newNetwork();

        List<String> args = new ArrayList<>();
        //args.add("-p 6379:6379");
        this.withNetwork(network);
        this.withCommand(args.toArray(new String[0]));
    }
}