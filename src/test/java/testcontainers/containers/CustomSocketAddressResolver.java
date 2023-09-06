package testcontainers.containers;

import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.DnsResolver;
import io.lettuce.core.resource.SocketAddressResolver;
import org.testcontainers.DockerClientFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomSocketAddressResolver extends SocketAddressResolver {

    private Map<Integer, Integer> redisClusterNatPortMapping;
    private final Map<Integer, SocketAddress> redisClusterSocketAddresses = new ConcurrentHashMap<>();

    protected CustomSocketAddressResolver(DnsResolver dnsResolver) {
        super(dnsResolver);
    }

    public CustomSocketAddressResolver(Map<Integer, Integer> redisClusterNatPortMapping) {
        super(DnsResolver.unresolved());
        this.redisClusterNatPortMapping = redisClusterNatPortMapping;
    }

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
}
