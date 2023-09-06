import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class CaasClientTester {
    public void main(String[] args) {
        String SCAN_LOCATOR_FIELD_SEPARATOR = "\\|";
        String test = "ABC" + "|" + "DEF";
        System.out.println(Arrays.toString(test.split(SCAN_LOCATOR_FIELD_SEPARATOR)));
        CaasClientTester caasClientTester = new CaasClientTester();
        Person<String> p = new Person<>();
        Function<Boolean, Void> f = this::get;

    }
    


    public Void get(boolean b) {
        return null;
    }


    static class Person<T> {

    }
}
