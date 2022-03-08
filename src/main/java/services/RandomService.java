package services;

import java.util.concurrent.CompletableFuture;

public class RandomService {


    public CompletableFuture<String> call() {
        CompletableFuture<String> cf = new CompletableFuture<>();

        Runnable action = () -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cf.complete("result");
        };
        new Thread(action).start();
        return cf;
    }
}
