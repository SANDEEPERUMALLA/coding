import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Tester3 {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("test");
                }
                //
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("testdone");
        });

//
//        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> {
//            System.out.println("456");
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("456done");
//        });

//
//        CompletableFuture<Void> cf3 = CompletableFuture.runAsync(() -> {
//            System.out.println("789");
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            throw new RuntimeException("exception789");
//        });

//        CompletableFuture.anyOf(List.of(cf1, cf2, cf3).toArray(new CompletableFuture[0])).exceptionally(e -> {
//            System.out.println("exceptionally" + e.getMessage());
////            List.of(cf1, cf2, cf3).forEach(cf -> {
////                cf.cancel(true);
////            });
//            return null;
//        }).join();
//        cf1.cancel(true);
        try {
            Thread.sleep(5000);
            boolean cancel = cf1.cancel(true);
            System.out.println("cancelled:" + cancel);
//            cf1.get();
            System.out.println("done");
        } catch (Exception e) {
            System.out.println(ExceptionUtils.getStackTrace(e));
        }

    }
}
