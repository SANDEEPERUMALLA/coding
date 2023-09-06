package com;

import java.lang.reflect.AnnotatedType;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class EnumTester {

    public enum T {
        C, A, B, D;
    }

    private static CompletableFuture<String> getCF()
    {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        return CompletableFuture.completedFuture("f3");
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {

        CompletableFuture<String> f1 = CompletableFuture.completedFuture("f1").thenApply(r -> {
                    System.out.println(r);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "f2";
                })
                .thenCompose(s -> {
                    return getCF();
                });
//                .thenApply(r -> {
//            System.out.println(r);
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                System.out.println("RuntimeException");
//                throw new RuntimeException();
//
//            }
//
//            return "f3";
//        });

        Thread.sleep(7000);
        f1.cancel(false);
        System.out.println("Resu;t" + f1.get(20, TimeUnit.SECONDS));

    }
}
