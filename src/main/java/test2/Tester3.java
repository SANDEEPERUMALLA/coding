package test2;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Tester3 {

    public static void main(String[] args) {
        String str = "vegacache-svc-demo1.caching-services.core1.perf-useast2.vegacachegovernanceperftestnamespace1";
        IntStream.rangeClosed(1, 8).forEach(i -> {
            IntStream.rangeClosed(1, 400).forEach(j -> {
                System.out.println("vegacache-svc-demo" + i + ".caching-services.core1.perf-useast2" + ":vegacachegovernanceperftestnamespace" + j);
            });
        });

        System.out.println(str);
    }

    public static void main1(String[] args) throws InterruptedException, ExecutionException {

        ExecutorService service = Executors.newFixedThreadPool(1);

        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    try {
                        Thread.sleep(2000);
                        System.out.println("test" + System.currentTimeMillis());
                        System.out.println(Thread.currentThread().isInterrupted());
                    } catch (Exception e) {
                        System.out.println("inexfgrg");
                        e.printStackTrace();
                        System.out.println("inexfgrg" + Thread.currentThread().isInterrupted());
                    }


                }
                //
            } catch (Throwable e) {
                System.out.println(Thread.currentThread().isInterrupted());
                e.printStackTrace();
            }
            System.out.println("testdone");
        }, service).whenComplete((d,ex) -> {
            System.out.println("exceptionally");
        });

        try {
            Thread.sleep(5000);
//            boolean cancel = cf1.cancel(true);
//            System.out.println("cancelled:" + cancel);
            cf1.completeExceptionally(new RuntimeException("test"));
            cf1.get();
        } catch (Exception e) {
            System.out.println("caught");
            e.printStackTrace();
            Thread.sleep(24524534);
            System.out.println(e.getCause());
            System.out.println(ExceptionUtils.getStackTrace(e));
        }
        System.out.println("done1");

    }
}
