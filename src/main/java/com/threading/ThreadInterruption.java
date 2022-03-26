package com.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadInterruption {


    public static void main(String[] args) throws InterruptedException {
        Runnable r = new SampleThread();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.submit(r);
        Thread.sleep(5000);
        executorService.shutdownNow();
        executorService.awaitTermination(2000, TimeUnit.SECONDS);

    }
}
