//package test2;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.concurrent.Callable;
//
//public class Task implements Callable<String> {
//    private final String id;
//
//    public Task(String id) {
//        this.id = id;
//    }
//
//    @Override
//    public String call() throws Exception {
//        int i = 0;
//
//
//        Duration.between(Instant.now() - )
//        while (true){
//            if(Thread.currentThread().isInterrupted()) {
//                break;
//            }
//            System.out.println(this.id + ":" + i++);
//        }
//        return null;
//    }
//}
