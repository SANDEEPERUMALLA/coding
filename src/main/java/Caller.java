import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Caller {

    public static void main(String[] args) {


        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        int interval = 10; // interval in seconds
        int n = 10; // number of times to execute the task
        for (int i = 0; i < n; i++) {
            executor.schedule(
                    () -> System.out.println("Executed : " + LocalDateTime.now()),
                    interval * i, TimeUnit.SECONDS);
        }
    }
}
