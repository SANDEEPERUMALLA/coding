package pcissue;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Tester {

    public static void main(String[] args) {
        // String[] buckets = new String[]{"BUCKET_1", "BUCKET_2", "BUCKET_3" , "BUCKET_4", "BUCKET_5", "BUCKET_6", "BUCKET_7"};
        String[] buckets = new String[]{"1_BUCKET", "2_BUCKET", "3_BUCKET", "4_BUCKET", "5_BUCKET", "6_BUCKET", "7_BUCKET"};
        PlatformCacheBucketMapper platformCacheBucketMapper = new PlatformCacheBucketMapper(buckets);

        Map<String, AtomicInteger> m = new HashMap<>();
//        for (int i = 0; i <= 100; i++) {

            for (int j = 0; j <= 20; j++) {
                String pat = "Partiton" + j;
//                String key = RandomStringUtils.randomAlphabetic(40);
                System.out.println("Key:" + pat);
                String bucket = platformCacheBucketMapper.selectRoute(pat).get(0);
                m.computeIfAbsent(bucket, (k) -> new AtomicInteger()).incrementAndGet();
            }

//        }
        System.out.println(m);
    }
}
