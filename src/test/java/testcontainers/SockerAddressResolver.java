package testcontainers;

import io.lettuce.core.resource.DnsResolver;
import io.lettuce.core.resource.SocketAddressResolver;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SockerAddressResolver extends SocketAddressResolver {


    private static ConcurrentMap<Integer, SocketAddress> redisClusterSocketAddresses = new ConcurrentHashMap<>();


    protected SockerAddressResolver(DnsResolver dnsResolver) {
        super(dnsResolver);
    }
}
