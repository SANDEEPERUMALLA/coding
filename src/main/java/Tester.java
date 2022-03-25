import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;


public class Tester {

    public CompletableFuture<String> get() {
        return CompletableFuture.completedFuture("test");
    }

    public static void testNavigationSet1() {
        NavigableSet<String> ns = new TreeSet<>();

        long start = System.nanoTime();
        for (int i = 1; i <= 3_000_000; i++) {
            ns.add(i + generateRandomStringOfSize(30));
        }

        System.out.println("Size : " + ns.size());
        System.out.println("Insert Time : " + (System.nanoTime() - start));

        List<Long> times = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now();
        for (int i = 1; i <= 9; i++) {
            start = System.nanoTime();
            NavigableSet<String> result = ns.subSet(i + "001", true, i + "010", true);
            System.out.println("Result Size: " + result.size());
            long time = System.nanoTime() - start;
            times.add(time);
            System.out.println("Time : " + time);
        }

        IntSummaryStatistics statistics = times.stream().mapToInt(Long::intValue).summaryStatistics();
        System.out.println(statistics.getAverage());
        System.out.println(statistics.getMax());
    }

    public static void main1(String[] args) {
        testNavigationSet1();
        //testNavigationSet2();
    }


    private static void testNavigationSet2() {
        List<String> dates = List.of("21/03/2022", "22/03/2022", "23/03/2022", "24/03/2022");

        List<String> finalValuesList = new ArrayList<>();
        dates.forEach(date -> {
            for (int i = 1; i <= 10; i++) {
                finalValuesList.add(date + ":" + generateRandomStringOfSize(15));
            }
        });

        System.out.println(finalValuesList);
        long start = System.nanoTime();
        NavigableSet<String> ns = new TreeSet<>(finalValuesList);

        System.out.println("Time : " + (System.nanoTime() - start));

        start = System.nanoTime();
        System.out.println(ns.subSet("22/03/2022", true, "24/03/2022", true));
        System.out.println("Time : " + (System.nanoTime() - start));

    }

    private static String generateRandomStringOfSize(int size) {
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder str = new StringBuilder();
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        for (int i = 1; i <= size; i++) {
            str.append(s.charAt(threadLocalRandom.nextInt(0, s.length() - 1)));
        }
        return str.toString();
    }

    public static List<String> main(String[] args) {
        LocalDateTime dateTime = LocalDateTime.now();
        List<String> dateStrings = new ArrayList<>();
        for (int i = 0; i <= 300; i++) {
            dateStrings.add(dateTime.getDayOfMonth() + "/" + dateTime.getMonth().getValue() + "/" + dateTime.getYear());
            dateTime = dateTime.minusDays(1);
        }


        return dateStrings;
    }

}
