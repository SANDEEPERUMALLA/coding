package test2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static final String DATE_FORMAT = "yyyy/MM/dd";

    public static void main(String[] args) throws InterruptedException {
        // localDateTest();

        String opList = "SCAN_FOR_COUNT_SR,SCAN_FOR_KEY_VALUES_SR,SCAN_FOR_MORE_KEY_VALUES_SR";
        List<String> collect = Arrays.stream(opList.split(",")).collect(Collectors.toList());
        System.out.println(collect);
    }

    public static void localDateTest() {
        LocalDate localDate = LocalDate.parse("1500/01/01", DateTimeFormatter.ofPattern(DATE_FORMAT));
        System.out.println(localDate);
        System.out.println(localDate.plusDays(10000));
    }

}
