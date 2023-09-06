package redis;

import com.salesforce.sds.keystore.DynamicKeyStoreBuilder;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class LettuceClient {

    public static void test() throws ExecutionException, InterruptedException {
        RedisURI redisURI = RedisURI.builder().withHost("localhost").withPort(6379).build();
        RedisClient redisClient = RedisClient.create(redisURI);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> async = connection.async();
        System.out.println(async.objectIdletime("k2").get());
        System.out.println(async.objectIdletime("k3").get());
//        RedisFuture<String> setCmd = async.set("k1", "v1");
//        System.out.println(async.objectIdletime("k2").get());
//        try {
//            setCmd.get();
//        } catch (InterruptedException | ExecutionException e) {
//            System.out.println(e.getMessage());
//            System.out.println("caught");
//            System.out.println(e.getCause() instanceof  RedisCommandExecutionException);;
//        }

        // System.out.println(Boolean.parseBoolean(null));

    }

    public static void main(String[] args) throws Exception {
        test();
    }
    public static final String KEYSTORE_FILE_NAME = "/ks.pkcs12";
    public static final String TRUSTSTORE_FILE_NAME = "/ts.pkcs12";

    public static void redis() throws Exception {

        String keyStoreFile = "/tmp/dktool_repo_falcontest/user/client" + KEYSTORE_FILE_NAME;
        String trustStoreFile = "/tmp/dktool_repo_falcontest/user/client" + TRUSTSTORE_FILE_NAME;
        deleteFile(keyStoreFile);
        deleteFile(trustStoreFile);
        String keyStoreFilePassword = RandomStringUtils.randomAlphanumeric(10);
        String trustStoreFilePassword = RandomStringUtils.randomAlphanumeric(10);

        new DynamicKeyStoreBuilder().withMonitoredDirectory("/tmp/dktool_repo_falcontest/user/client").withStoreFilename(keyStoreFile)
                .withStorepass(keyStoreFilePassword).withKeyPassword(keyStoreFilePassword)
                .withTruststoreFilename(trustStoreFile).withTruststorePass(trustStoreFilePassword)
                .withCADirectory("/tmp/dktool_repo_falcontest/ca").withFlushToDisk(true).withStartThread(true)
                .withoutChmod().build();

        RedisURI redisURI = RedisURI.builder()
                .withHost("localhost")
                .withPort(6379)
                .withSsl(true)
                .withVerifyPeer(true)
                .build();

        RedisClient redisClient = RedisClient.create(redisURI);
        SslOptions sslOptions = SslOptions.builder()
                .jdkSslProvider()
                .keystore(new File(keyStoreFile), keyStoreFilePassword.toCharArray())
                .truststore(new File(trustStoreFile), trustStoreFilePassword)
                .build();

        ClientOptions clientOptions = ClientOptions.builder().sslOptions(sslOptions).build();
        redisClient.setOptions(clientOptions);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.set("k1", "v1");
        System.out.println(syncCommands.get("k1"));
    }


    private static void deleteFile(String file) throws Exception {
        try {
            File f = new File(file);
            if (f != null && f.exists()) {
                if (f.delete()) {
                } else {
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
}
