import redis.clients.jedis.Jedis;

import static io.lettuce.core.cluster.SlotHash.SLOT_COUNT;

public class Tester2 {

    public static void main(String[] args) {

        Jedis jedis = new Jedis("localhost", 6379);
        for (int i = 0; i < SLOT_COUNT; i++) {
            jedis.clusterAddSlots(i);
        }

//
//        Jedis jedis = new Jedis("localhost", 6379);
//        int index = 1;
//        for (int i = 1; i <= 200; i++) {
//            for (int j = 1; j <= 10; j++) {
////                Thread.sleep(1000);
//                jedis.set("ora1:caas::ORG" + j + ":key" + index, "value");
//                index++;
//            }
//        }

    }

    private static String sanitizeKey(String key) {
        key = key.replace("{", "");
        return key.replace("}", "");
    }
}
