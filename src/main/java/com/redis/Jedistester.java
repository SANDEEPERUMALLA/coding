package com.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.commands.ProtocolCommand;

import java.util.Arrays;
import java.util.regex.Pattern;

import static redis.clients.jedis.util.SafeEncoder.DEFAULT_CHARSET;

public class Jedistester {

    public static class ReplicaConfCommand implements ProtocolCommand {

        @Override
        public byte[] getRaw() {
            return "REPLCONF".getBytes(DEFAULT_CHARSET);
        }
    }

    public static void main(String[] args) {
//        ReplicaConfCommand cmd = new ReplicaConfCommand();
//        Jedis jedis = new Jedis("localhost", 6379);
//        Object o = jedis.sendCommand(cmd, "ACK".getBytes(DEFAULT_CHARSET), "0".getBytes(DEFAULT_CHARSET));
//
//
//        System.out.println(o);
//        String str = "core.ora3:testns1::";
//        String[] keyTokensArray = str.split(":" );
//        System.out.println(keyTokensArray.length);
//        System.out.println(Arrays.toString(keyTokensArray));
        String str = "testsn.testsi:ns1:-:ORG1";
        System.out.println(Pattern.compile("testsn.testsi").matcher(str).find());
    }

}
