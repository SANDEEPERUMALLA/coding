import java.util.List;
import java.util.concurrent.ExecutionException;


public class ShardKeyGenerator {

    private static final String[] DIGITS_NAME_ARRAY = new String[]{"zero", "one", "two", "three", "four", "five",
            "six", "seven", "eight", "nine"};

    private static String getString(int i) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(DIGITS_NAME_ARRAY[i % 10]);
        } while ((i /= 10) > 0);
        return sb.toString();
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        List<String> partitionNames = List.of("00DG0000000CLUj:local:imssessionpartition:o",
                "00DG0000000CLUj:local:imssessionpartition:s",
                "00DG0000000CLUj:local:workspacesession:s",
                "00DG0000000CLUj:vlocity_ins:vlocityapiresponse:s",
                "00DG0000000CLUj:vlocity_ins:vlocitymetadata:o");

        int noOfBuckets = 40;
        partitionNames.forEach(pName -> {
            String[] partitionParts = pName.split(":", -1);
            String transformedPartitionName = String.join(":", partitionParts[2], partitionParts[3], partitionParts[1], partitionParts[0]);
            for (int i = 0; i < noOfBuckets; i++) {
                String shardKey = getString(i) + ":" + transformedPartitionName;
                System.out.println(shardKey);
            }
        });
    }


}
