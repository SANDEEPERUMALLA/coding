package com.navigableset.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import static com.navigableset.test.LoadClient.getDateString;
import static com.navigableset.test.logging.Logger.log;

public class NavigableSetUser implements User {

    private final NavigableSet<String> navigableSet;
    private final int userId;
    private long noOfOps = 0;
    List<Long> latencies = new ArrayList<>();

    public NavigableSetUser(NavigableSet<String> navigableSet, int userId) {
        this.navigableSet = navigableSet;
        this.userId = userId;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(String.valueOf(this.userId));
        while (!Thread.currentThread().isInterrupted()) {
            LocalDateTime dateTime = LocalDateTime.now();
            for (int i = 1; i <= 50; i++) {
                String eR = getDateString(dateTime);
                dateTime = dateTime.minusDays(1);
                String sR = getDateString(dateTime);
              //  log("Range : [" + sR + " - " + eR + "]");
                long start = System.nanoTime();
                NavigableSet<String> result = navigableSet.subSet(sR, true, eR, true);
                long time = System.nanoTime() - start;
                noOfOps++;
              //  log("NS subset operation time: " + time);
                latencies.add(time);
               // log("Result Size : " + result.size());
            }
        }
    }

    @Override
    public long getNoOfOps() {
        return noOfOps;
    }

    @Override
    public List<Long> getLatencies() {
        return latencies;
    }

    @Override
    public String getName() {
        return "user" + userId;
    }
}
