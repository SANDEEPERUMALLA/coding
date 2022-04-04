import com.google.common.collect.ImmutableMap;

public class GauvaTest {

    public static void main(String[] args) {

        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put("key1", "val1");
        builder1.put("key2", "val2");
        builder1.put("key3", "val3");
        builder1.put("key4", "val4");
        ImmutableMap<String, String> map1 = builder1.build();

        ImmutableMap.Builder<String, String> builder2 = ImmutableMap.builder();
        builder2.put("key3", "val3");
        builder2.put("key2", "val2");
        builder2.put("key4", "val4");
        builder2.put("key1", "val1");
        ImmutableMap<String, String> map2 = builder2.build();

        System.out.println(map1.equals(map2));
    }
}
