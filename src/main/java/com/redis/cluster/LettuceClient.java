package com.redis.cluster;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LettuceClient {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        InetSocketAddress node1 = new InetSocketAddress("localhost", 12000);
        InetSocketAddress node2 = new InetSocketAddress("localhost", 12001);
        InetSocketAddress node3 = new InetSocketAddress("localhost", 12002);
        InetSocketAddress node4 = new InetSocketAddress("localhost", 12003);
        InetSocketAddress node5 = new InetSocketAddress("localhost", 12004);
        InetSocketAddress node6 = new InetSocketAddress("localhost", 12005);
        RedisURI uri1 = RedisURI.create(node1.getAddress().getHostAddress(), 12000);
        RedisURI uri2 = RedisURI.create(node2.getAddress().getHostAddress(), 12001);
        RedisURI uri3 = RedisURI.create(node3.getAddress().getHostAddress(), 12002);
        RedisURI uri4 = RedisURI.create(node4.getAddress().getHostAddress(), 12003);
        RedisURI uri5 = RedisURI.create(node5.getAddress().getHostAddress(), 12004);
        RedisURI uri6 = RedisURI.create(node6.getAddress().getHostAddress(), 12005);

        ClusterClientOptions.Builder clusterClientOptionsBuilder = ClusterClientOptions.builder()
                .autoReconnect(true)
                .protocolVersion(ProtocolVersion.RESP3)
                .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                        .enablePeriodicRefresh(Duration.ofSeconds(30))
                        .dynamicRefreshSources(true)
                        .enableAllAdaptiveRefreshTriggers()
                        .build());

        ClusterClientOptions clusterClientOptions = clusterClientOptionsBuilder.build();


        RedisClusterClient client1 = RedisClusterClient.create(uri1);
        client1.setOptions(clusterClientOptions);
        client1.refreshPartitions();
        RedisClusterClient client2 = RedisClusterClient.create(uri2);
        RedisClusterClient client3 = RedisClusterClient.create(uri3);
        RedisClusterClient client4 = RedisClusterClient.create(uri4);
        RedisClusterClient client5 = RedisClusterClient.create(uri5);
        RedisClusterClient client6 = RedisClusterClient.create(uri6);
        client1.refreshPartitions();
        client2.refreshPartitions();
        client3.refreshPartitions();
        client4.refreshPartitions();
        client5.refreshPartitions();
        client6.refreshPartitions();

        StatefulRedisClusterConnection<String, String> connect = client1.connect();
        connect.setReadFrom(ReadFrom.NEAREST);
        RedisAdvancedClusterCommands<String, String> commands = connect.sync();
        System.out.println(commands.get("key1"));

//        CompletableFuture<RedisAsyncCommands<String, String>> commandFuture1 = client1.connectAsync(StringCodec.UTF8)
//                .thenCompose(a -> a.getConnectionAsync(node1.getAddress().getHostAddress(), 12000))
//                .thenApply(StatefulRedisConnection::async);
//        CompletableFuture<RedisAsyncCommands<String, String>> commandFuture2 = client2.connectAsync(StringCodec.UTF8)
//                .thenCompose(a -> a.getConnectionAsync(node2.getAddress().getHostAddress(), 12001))
//                .thenApply(StatefulRedisConnection::async);
//        CompletableFuture<RedisAsyncCommands<String, String>> commandFuture3 = client3.connectAsync(StringCodec.UTF8)
//                .thenCompose(a -> a.getConnectionAsync(node3.getAddress().getHostAddress(), 12002))
//                .thenApply(StatefulRedisConnection::async);
//        CompletableFuture<RedisAsyncCommands<String, String>> commandFuture4 = client4.connectAsync(StringCodec.UTF8)
//                .thenCompose(a -> a.getConnectionAsync(node4.getAddress().getHostAddress(), 12003))
//                .thenApply(StatefulRedisConnection::async);
//        CompletableFuture<RedisAsyncCommands<String, String>> commandFuture5 = client5.connectAsync(StringCodec.UTF8)
//                .thenCompose(a -> a.getConnectionAsync(node5.getAddress().getHostAddress(), 12004))
//                .thenApply(StatefulRedisConnection::async);
//        CompletableFuture<RedisAsyncCommands<String, String>> commandFuture6 = client6.connectAsync(StringCodec.UTF8)
//                .thenCompose(a -> a.getConnectionAsync(node6.getAddress().getHostAddress(), 12005))
//                .thenApply(StatefulRedisConnection::async);
//
//
//        CompletableFuture<String> stringCompletableFuture = commandFuture1.thenCompose(command -> command.set("fwrfrgreg", "v1"));
//        stringCompletableFuture.whenComplete((r,ex) -> {
//            System.out.println("frfr");
//        });

//        CompletableFuture<String> f = commandFuture1.thenCompose(command -> command.clusterMeet(node1.getAddress().getHostAddress(), node1.getPort()));
//        CompletableFuture<String> f1 = commandFuture1.thenCompose(command -> command.clusterMeet(node2.getAddress().getHostAddress(), node2.getPort()));
//        CompletableFuture<String> f2 = commandFuture1.thenCompose(command -> command.clusterMeet(node3.getAddress().getHostAddress(), node3.getPort()));
//        CompletableFuture<String> f3 = commandFuture1.thenCompose(command -> command.clusterMeet(node4.getAddress().getHostAddress(), node4.getPort()));
//        CompletableFuture<String> f4 = commandFuture1.thenCompose(command -> command.clusterMeet(node5.getAddress().getHostAddress(), node5.getPort()));
//        CompletableFuture<String> f5 = commandFuture1.thenCompose(command -> command.clusterMeet(node6.getAddress().getHostAddress(), node6.getPort()));
//
//        List<CompletableFuture<?>> cfList = new ArrayList<>();
//        cfList.add(f);
//        cfList.add(f1);
//        cfList.add(f2);
//        cfList.add(f3);
//        cfList.add(f4);
//        cfList.add(f5);


//        CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0])).whenComplete((r,ex) -> {
//            try {
//                commandFuture1.get().clusterNodes().thenAccept(r1->{
//                    System.out.println(r1);
//                });
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        });



    }
}
