package com.estimate_java_object_size;

import com.RedisNode;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class T {

    public static final String DATE_TIME_FORMAT = "yyyy/MM/dd:HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";

    public static void main(String[] args) {
        List<RedisNode> l1 = new ArrayList<>();
        l1.add(new RedisNode("test", 10));
        l1.add(new RedisNode("test1", 20));
        l1.add(new RedisNode("test3", 30));

        List<RedisNode> l2 = new ArrayList<>();
        l2.add(new RedisNode("test", 10));
        l1.add(new RedisNode("test1", 20));
        l2.add(new RedisNode("test4", 30));

        System.out.println(CollectionUtils.removeAll(l2, l1));
        System.out.println();

    }
}
