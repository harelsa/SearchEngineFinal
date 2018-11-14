package Engine.Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedMap;

public class PostingList {
    SortedMap<Term, HashSet<PostingListEntry>> postingList;

    public void addPostingEntry(Term term, Document doc) {
        PostingListEntry newEntry = new PostingListEntry(doc);
        if (postingList.containsKey(term)) {
            postingList.get(term).add(newEntry);
        }
        else{
            HashSet<PostingListEntry> entries = new HashSet<>();
            entries.add(newEntry);
            postingList.put(term, entries);
        }
    }
}

