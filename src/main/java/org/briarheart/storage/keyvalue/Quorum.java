package org.briarheart.storage.keyvalue;

import org.briarheart.storage.keyvalue.util.Strings;

/**
 * @author Roman Chigvintsev
 */
public class Quorum {
    private static final Quorum DEFAULT = new Quorum();

    private final int minNumberOfVotes;
    private final int numberOfReplicas;

    public Quorum() {
        this(-1, -1);
    }

    public Quorum(int minNumberOfVotes, int numberOfReplicas) {
        this.minNumberOfVotes = minNumberOfVotes;
        this.numberOfReplicas = numberOfReplicas;
    }

    public int getMinNumberOfVotes() {
        return minNumberOfVotes;
    }

    public int getNumberOfReplicas() {
        return numberOfReplicas;
    }

    public static Quorum parse(String s) {
        if (Strings.isNullOrEmpty(s))
            return DEFAULT;
        String[] split = s.split("/");
        int minNumberOfVotes = Integer.parseInt(split[0]);
        int numberOfReplicas = split.length > 1 ? Integer.parseInt(split[1]) : -1;
        return new Quorum(minNumberOfVotes, numberOfReplicas);
    }
}
