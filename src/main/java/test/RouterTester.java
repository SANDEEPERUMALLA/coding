package test;

import org.apache.commons.lang3.RandomStringUtils;

public class RouterTester {

    public static void main(String[] args) {
        String[] shards = new String[]{"BUCKET_1", "BUCKET_2", "BUCKET_3", "BUCKET_4", "BUCKET_5", "BUCKET_6"};
        BucketMapper bucketMapper = new BucketMapper(shards);
        for (int i = 1; i <= 2000; i++) {
            String key = RandomStringUtils.randomAlphabetic(10);
            String shard = bucketMapper.selectRoute(key).get(0);
            System.out.println(shard);
        }
    }
}
