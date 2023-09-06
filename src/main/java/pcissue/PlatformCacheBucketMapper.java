package pcissue;

import com.force.commons.util.DispersiveRouter;

public class PlatformCacheBucketMapper extends DispersiveRouter<String, String> {

    public PlatformCacheBucketMapper(String[] providedPeers) {
        super(providedPeers);
    }

    @Override
    protected int hashHint(String key) {
        return key.hashCode();
    }

}
