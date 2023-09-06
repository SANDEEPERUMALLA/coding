//package com.vegacache;
//
//import com.salesforce.vegacache.client.*;
//import com.salesforce.vegacache.client.api.CacheSyncApi;
//import com.salesforce.vegacache.client.impl.CacheManagerBuilder;
//import com.salesforce.vegacache.client.impl.CacheManagerConfigurationBuilder;
//import com.salesforce.vegacache.client.impl.SimpleCacheKey;
//import com.salesforce.vegacache.client.namespace.InProcessNamespace;
//import com.salesforce.vegacache.client.namespace.InProcessNamespaceConfiguration;
//import com.salesforce.vegacache.client.namespace.NamespaceFQDN;
//import com.salesforce.vegacache.client.namespace.NamespaceType;
//import com.salesforce.vegacache.client.namespace.builders.InProcessNamespaceConfigurationBuilder;
//import com.salesforce.vegacache.client.namespace.builders.NamespaceFQDNBuilder;
//import com.salesforce.vegacache.client.serializer.DefaultSerializer;
//
//import java.net.InetSocketAddress;
//import java.time.Duration;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//public class VegaCacheTester {
//
//    protected static <K extends CacheKey, V> InProcessNamespaceConfiguration<K, V> getInProcessNamespaceConfiguration(
//            Class<K> keyType, Class<V> valueType) {
//        return getInProcessNamespaceConfigurationBuilder(keyType, valueType).build();
//    }
//
//    protected static <K extends CacheKey, V> InProcessNamespaceConfigurationBuilder<K, V> getInProcessNamespaceConfigurationBuilder(
//            Class<K> keyType, Class<V> valueType) {
//        return getInProcessNamespaceConfigurationBuilder(keyType, valueType, Duration.ofSeconds(5),
//                Duration.ofDays(1), new DefaultSerializer<>(), false, 500L, 50, 1024,
//                true, Executors.newFixedThreadPool(5));
//    }
//
//    protected static <K extends CacheKey, V> InProcessNamespaceConfigurationBuilder<K, V> getInProcessNamespaceConfigurationBuilder(
//            Class<K> keyType, Class<V> valueType, Duration waitTimeOut, Duration timeToLiveForCreation,
//            Serializer<V> serializer, boolean noThrowException, long maxL1TotalSizeBytes, int maxL1EntrySizeBytes,
//            int compressionThreshold, boolean statsEnabled, Executor executor) {
//        InProcessNamespaceConfigurationBuilder<K, V> enamespace = InProcessNamespaceConfigurationBuilder.getInProcessNamespaceConfigurationBuilder(
//                keyType, valueType);
//        enamespace = noThrowException ?
//                (InProcessNamespaceConfigurationBuilder) enamespace.throwExceptionOnError() :
//                (InProcessNamespaceConfigurationBuilder) enamespace.noThrowExceptionOnError();
//        return (InProcessNamespaceConfigurationBuilder) ((InProcessNamespaceConfigurationBuilder) ((InProcessNamespaceConfigurationBuilder) ((InProcessNamespaceConfigurationBuilder) ((InProcessNamespaceConfigurationBuilder) ((InProcessNamespaceConfigurationBuilder) ((InProcessNamespaceConfigurationBuilder) ((InProcessNamespaceConfigurationBuilder) enamespace.withWaitTimeout(
//                waitTimeOut)).withTimeToLiveForCreation(timeToLiveForCreation)).withSerializer(
//                serializer)).withMaxL1TotalSizeBytes(maxL1TotalSizeBytes)).withMaxL1EntrySizeBytes(
//                maxL1EntrySizeBytes)).withCompressionThresholdBytes(compressionThreshold)).withStatisticsEnabled(
//                statsEnabled)).withExecutor(executor);
//    }
//
//    public static void main(String[] args) {
//        ClientId clientId = ClientIdBuilder.newClientIdBuilder().withServiceName("sn")
//                .withServiceInstance("si").build();
//        CacheManagerConfigurationBuilder builder = CacheManagerConfigurationBuilder.newCacheManagerConfigurationBuilder()
//                .withMetricsConfiguration(new MetricsFactoryImpl(), new MetricsRegistryImpl(
//                        new MetricsConfiguration("mysp", "landmark", "mypod", false, "http://funnel:8080",
//                                "caas-client-test", "com.salesforce.caas-client-test"))).withL1Memory(10000000);
//        builder.withMetricsConfiguration(new MetricsFactoryImpl(), new MetricsRegistryImpl(
//                new MetricsConfiguration("mysp", "landmark", "mypod", false, "http://funnel:8080", "caas-client-test",
//                        "com.salesforce.caas-client-test")));
//        builder.addCaaSEndpoint(new InetSocketAddress("localhost", 6379));
//        CacheManagerConfiguration cacheManagerConfiguration = builder.build();
//        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder(clientId, cacheManagerConfiguration)
//                .build();
//        NamespaceFQDN namespaceFQDN = NamespaceFQDNBuilder.newNamespaceFQDNBuilder(clientId, "namespace")
//                .withSubNamespace("subns").build();
//        InProcessNamespaceConfiguration<SimpleCacheKey, String> inProcessNamespaceConfiguration = getInProcessNamespaceConfiguration(
//                SimpleCacheKey.class, String.class);
//        InProcessNamespace<SimpleCacheKey, String> inProcessNamespace = cacheManager.getInProcessNamespace(
//                namespaceFQDN, inProcessNamespaceConfiguration);
//        CacheSyncApi<SimpleCacheKey, String> cacheSyncApi = inProcessNamespace.getCacheSyncApi();
//        SimpleCacheKey k = SimpleCacheKey.getSimpleCacheKey("k");
//        cacheSyncApi.put(k, "v");
//        for (int i = 1; i <= 100000000; i++) {
//            // long s = System.nanoTime();
//            String val = cacheSyncApi.get(k);
//            // long e = System.nanoTime();
//            System.out.println(val);
//        }
//    }
//
//}
//
