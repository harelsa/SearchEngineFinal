package Engine.Model;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer {
    public static ConcurrentHashMap<Term, PostingList> dictionary;

    public Indexer(SegmentFilePartition[] segmentFilePartitions) {

    }

    public void addSegmentPartitionFile(SegmentFilePartition segmentFilePartitions) {
    }
}
