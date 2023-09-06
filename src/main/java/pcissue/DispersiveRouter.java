/**
 *
 */
package pcissue;

import java.util.*;
import java.util.Map.Entry;

/**
 * Implements a dispersive router based loosely on the Cache Array Routing
 * Protocol ({@link http://icp.ircache.net/carp.txt}) to provide equitable
 * routing to a number of peers that is deterministic, based on a given request.
 * By providing this functionality, caches can be used more effectively by
 * consistently routing to the same ranked peer list.
 *
 * Note that before applying this class to a new domain, verify its dispersion
 * performance over peer and request tokens in that domain by extending the
 * {@link DispersiveRouterTest}.
 *
 * This implementation uses Java's djb2 hash which performs better over the
 * types of strings that we are using. Specifically, with CARP, Strings with
 * close proximity transpositions, or identical character summations wind up
 * with the same hash code (e.g. "2-4", "4-2", "3-3", all have the same hash).
 *
 * This implementation does not currently support peer weighting.
 *
 * @author paulc
 * @since 142.7
 */
public abstract class DispersiveRouter<H, P> {

    /**
     * The names of the peers we are interested in. Stored so that we can return
     * them in rank order. 
     */
    private final List<P> peers;
    
    static final class PeerAndHash<P> {

        private final P peer;
        
        /**
         * The 32 bit hash value for the peer.
         */
        private final int hash;
        
        private final Integer position;

        PeerAndHash(P peer, int hash, int position) {
            this.peer = peer;
            this.hash = hash;
            this.position = Integer.valueOf(position);
        }

        P getPeer() {
            return peer;
        }

        int getHash() {
            return hash;
        }

        Integer getPosition() {
            return position;
        }
    }
    
    private final ArrayList<PeerAndHash> peersAndHashes;

    // couldn't make this a static because of the generic P. maybe someone smarter about generics can pull it off.
    private final Comparator<? super Map.Entry<PeerAndHash<P>, Integer>> comparator = new Comparator<Map.Entry<PeerAndHash<P>, Integer>>() {
        
        @Override
        public int compare(Entry<PeerAndHash<P>, Integer> o1, Entry<PeerAndHash<P>, Integer> o2) {
            int score = o2.getValue().compareTo(o1.getValue());
            if (score != 0) return score;

            return o1.getKey().getPosition().compareTo(o2.getKey().getPosition());
        }
    };

    /**
     * A dispersion factor to greater spread the hashed value across the hash
     * space.
     */
    private static final int FACTOR = 0x62531965;

    /**
     * Creates a router to distribute requests amongst the given peers.
     */
    public DispersiveRouter(P[] providedPeers) {
        assert providedPeers.length > 0 : "A peer set must actually have members";
        
        this.peersAndHashes = new ArrayList<PeerAndHash>(providedPeers.length);

        int i = 0;
        for (P peer : providedPeers) {
            int hash = hashPeer(peer) * FACTOR;
            this.peersAndHashes.add(new PeerAndHash<P>(peer, hash, i));
            i++;
        }
        
        this.peers = Collections.<P>unmodifiableList(new ArrayList<P>(Arrays.asList(providedPeers)));
    }

    /**
     * Hashes the given request to find the rank ordering of peers to handle
     * that request. Ideally, different requests yeild different rankings,
     * however milage may vary. It is sensitive to domain, so please test over
     * characteristic data before using.
     *
     * @param request
     *            A H representing a request to be routed
     * @return A rank ordered list of peers that can handle the request provided, should never be null
     */
    public List<P> selectRoute(H request) {
        return selectRoute(request, peers.size());
    }
    
    /**
     * Hashes the given request to find the rank ordering of peers to handle
     * that request. Ideally, different requests yeild different rankings,
     * however milage may vary. It is sensitive to domain, so please test over
     * characteristic data before using.
     *
     * @param request
     *            A H representing a request to be routed
     * @param numPeers
     *            The maximum number of peers you want returned. Must be non-negative.
     * @return A rank ordered list of peers that can handle the request provided, should never be null
     */
    public List<P> selectRoute(H request, int numPeers) {
        if (numPeers < 0) throw new IllegalArgumentException("numPeers must be non-negative: " + numPeers);
        
        int hash = hashHint(request);
        
        ArrayList<Map.Entry<PeerAndHash<P>, Integer>> peersAndScores = new ArrayList<Map.Entry<PeerAndHash<P>, Integer>>(peers.size());
        
        for (PeerAndHash<P> p : this.peersAndHashes) {
            int combinedHash = hash ^ p.getHash();
            combinedHash += combinedHash * FACTOR;

            // we are creating an extra Map.Entry object per peer
            // we are boxing ints for the score
            peersAndScores.add(new AbstractMap.SimpleImmutableEntry<PeerAndHash<P>, Integer>(p, combinedHash));
        }
        Collections.sort(peersAndScores, this.comparator);
        
        List<P> matches = new ArrayList<P>(numPeers);
        for (int i = 0; i < numPeers; i++) {
            matches.add(peersAndScores.get(i).getKey().getPeer());
        }
        
        return matches;
    }

    protected List<P> getPeers() {
        return this.peers;       // already wrapped in an unmodifiableList
    }
    
    protected int hashPeer(P peer) {
        return hashChars(peer.toString());
    }
    
    protected abstract int hashHint(H hint);
    
    /**
     * A hash operation applied to peers and requests that is dispersive across
     * the hash space. Since the strings used in our app (host names, ids, etc.)
     * vary most in their last character, in an attempt to add more entropy to
     * those strings we prepend the last character. This improves performance on
     * the test cases in {@link DispersiveRouterTest}.
     *
     * @param str
     *            The String to be hashed
     * @return the hash code for the given string
     */
    public static int hashChars(String str) {
        return (str.charAt(str.length() - 1) + str).hashCode();
    }
}
