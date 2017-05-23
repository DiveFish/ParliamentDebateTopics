package compact;

import gnu.trove.list.TIntList;
import org.apache.commons.math3.util.Pair;

import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Patricia Fischer
 */
public class ClusterUtils {

    /**
     * Find the earliest document in the cluster. Sort documents by date and
     * return doc id with earliest date.
     *
     * @param dateIds The dates by document indices
     * @param cluster The cluster of documents
     * @return The doc id of earliest date in cluster
     */
    public static int earliestDoc(Map<Integer, Date> dateIds, TIntList cluster) {
        SortedSet<Pair<Integer, Date>> sortedDates = new TreeSet<>((o1, o2) -> {
            int cmp = o1.getValue().compareTo(o2.getValue());
            if (cmp == 0) {
                return o1.getKey().compareTo(o2.getKey());
            }
            return cmp;
        });

        for (int i = 0; i < cluster.size(); i++) {
            sortedDates.add(new Pair<>(cluster.get(i), dateIds.get(cluster.get(i))));
        }

        System.out.printf("Earliest document: %s, date: %s\n", sortedDates.first().getKey(), sortedDates.first().getValue());
        return sortedDates.first().getKey();
    }
}
