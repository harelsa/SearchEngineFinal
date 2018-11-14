package Engine.Model;

import java.util.concurrent.ConcurrentHashMap;

public class Indexer {
    ConcurrentHashMap<Term, PostingList> dictionary;
}
