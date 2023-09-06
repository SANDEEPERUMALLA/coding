package com.estimate_java_object_size;

import org.HdrHistogram.AtomicHistogram;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class InternTester {

    public static void main(String[] args) {

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 1_0000_000; i++) {
            list.add(RandomStringUtils.randomAlphanumeric(20));
        }

        AtomicHistogram histogram = new AtomicHistogram(1, 1000000000, 2);
        for (int i = 1; i <= 1_0000_000; i++) {
            long s = System.nanoTime();
            list.add(RandomStringUtils.randomAlphanumeric(20).intern());
            histogram.recordValue(System.nanoTime() - s);
        }

        System.out.println("P99: " + histogram.getValueAtPercentile(99));
        System.out.println("P95: " + histogram.getValueAtPercentile(95));
        System.out.println("P90: " + histogram.getValueAtPercentile(90));
        System.out.println("Mean: " + histogram.getMean());
        System.out.println("Count: " + histogram.getTotalCount());
    }
}
