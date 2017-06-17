import java.util.*;

/**
 * Created by dan on 17/06/17.
 */
public class AnchorProcessor {
    HashMap<Integer, HashSet<Integer>> anchorIndex;
    HashMap<String, Integer> anchorVocabulary;
    HashMap<Integer, String> anchorDocument;

    AnchorProcessor(HashMap<Integer, HashSet<Integer>> anchorIndex,
                    HashMap<String, Integer> anchorVocabulary,
                    HashMap<Integer, String> anchorDocument) {
        this.anchorIndex = anchorIndex;
        this.anchorVocabulary = anchorVocabulary;
        this.anchorDocument = anchorDocument;
    }

    public SortedMap<Double, HashSet<Integer>> updateRetrievedDocuments(String query,
            SortedMap<Double, HashSet<Integer>> retrievedDocuments,
            HashMap<Integer, String> document) {
        HashSet<String> anchorRetrievedDocument = new HashSet<>();
        String[] queryTerms = query.split(" ");
        Integer termId;
        for (String queryTerm : queryTerms) {
            termId = anchorVocabulary.get(queryTerm);
            if (!anchorIndex.containsKey(termId)) {
                continue;
            }

            for (Integer docId : anchorIndex.get(termId)) {
                anchorRetrievedDocument.add(anchorDocument.get(docId));
            }
        }

        SortedMap<Double, HashSet<Integer>> updatedRetrievedDocuments = new TreeMap<>(Collections.reverseOrder());
        Double newScore;
        Iterator it = retrievedDocuments.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            for (Integer link : (HashSet<Integer>) pair.getValue()) {
                if (anchorRetrievedDocument.contains(document.get(link))) {
                    newScore = (Double) pair.getKey() * 1.1;
                } else {
                    newScore = (Double) pair.getKey();
                }
                updatedRetrievedDocuments.putIfAbsent(newScore, new HashSet<>());
                updatedRetrievedDocuments.get(newScore).add(link);
            }
        }

        return  updatedRetrievedDocuments;
    }


}
