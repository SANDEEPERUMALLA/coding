package test;

import org.apache.commons.lang3.RandomStringUtils;

public class BucketMapperTester {
    public static void main(String[] args) {

        BucketMapper bucketMapper = new BucketMapper(new String[]{"s1", "s2", "s3", "s4", "s5", "s6"});
        System.out.println(bucketMapper.selectRoute("BUCKET_1").get(0));
        System.out.println(bucketMapper.selectRoute("BUCKET_2").get(0));
        System.out.println(bucketMapper.selectRoute("BUCKET_3").get(0));
        System.out.println(bucketMapper.selectRoute("BUCKET_4").get(0));
        System.out.println(bucketMapper.selectRoute("BUCKET_5").get(0));
        System.out.println(bucketMapper.selectRoute("BUCKET_6").get(0));

        String s = RandomStringUtils.randomAlphabetic(10);
        System.out.println(bucketMapper.selectRoute(s).get(0));
        System.out.println(s.hashCode());
        s = RandomStringUtils.randomAlphabetic(10);
        System.out.println(bucketMapper.selectRoute(s).get(0));
        System.out.println(s.hashCode());
        s = RandomStringUtils.randomAlphabetic(10);
        System.out.println(bucketMapper.selectRoute(s).get(0));
        System.out.println(s.hashCode());
        s = RandomStringUtils.randomAlphabetic(10);
        System.out.println(bucketMapper.selectRoute(s).get(0));
        System.out.println(s.hashCode());
        s = RandomStringUtils.randomAlphabetic(10);
        System.out.println(bucketMapper.selectRoute(s).get(0));
        System.out.println(s.hashCode());
        s = RandomStringUtils.randomAlphabetic(10);
        System.out.println(bucketMapper.selectRoute(s).get(0));
        System.out.println(s.hashCode());
        s = RandomStringUtils.randomAlphabetic(10);
        System.out.println(bucketMapper.selectRoute(s).get(0));
        System.out.println(s.hashCode());

    }
}
