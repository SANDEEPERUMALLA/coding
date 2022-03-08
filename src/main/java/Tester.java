import test.IService;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class Tester {

    public CompletableFuture<String> get() {
        return CompletableFuture.completedFuture("test");
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println(IService.var);

        List<String> l;
        List<?> l1  = new ArrayList<>();
        test((List<String>) l1);


    }

    public static void test(List<String> l) {

    }
}
