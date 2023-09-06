package test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFutureExample {
    public static void main(String[] args) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Future computation started.");
                TimeUnit.SECONDS.sleep(5); // Simulating a long-running task
                System.out.println("Future computation completed.");
            } catch (InterruptedException e) {
                System.out.println("Future computation interrupted.");
            }
        });

        try {
            TimeUnit.SECONDS.sleep(2); // Wait for 2 seconds

            // Cancel the future
            boolean isCancelled = future.cancel(true);
            System.out.println("Future cancelled: " + isCancelled);

            future.get(); // Waiting for the future to complete
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted.");
        } catch (ExecutionException e) {
            System.out.println("Exception occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getClass());
            System.out.println("Timeout occurred.");
        }
    }
}
