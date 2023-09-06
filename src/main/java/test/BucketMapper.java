package test;

public class BucketMapper extends DispersiveRouter<String,String> {
    /**
     * Creates a router to distribute requests amongst the given peers.
     *
     * @param providedPeers
     */
    public BucketMapper(String[] providedPeers) {
        super(providedPeers);
    }

    @Override
    protected int hashHint(String hint) {
        return hint.hashCode();
    }
}
