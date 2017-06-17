import java.util.*;

/**
 * Created by dan on 16/06/17.
 */
public class PageRankProcessor {
    private HashMap<Integer, Double> pageRank;

    PageRankProcessor(HashMap<Integer, Double> pageRank) {
        this.pageRank = pageRank;
    }


    public SortedMap<Double, HashSet<Integer>> updateRetrievedDocuments(
            SortedMap<Double, HashSet<Integer>> retrievedDocuments) {
        SortedMap<Double, HashSet<Integer>> updatedRetrievedDocuments = new TreeMap<>(Collections.reverseOrder());
        Double newScore;
        Iterator it = retrievedDocuments.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            for (Integer link : (HashSet<Integer>) pair.getValue()) {
                newScore = (Double) pair.getKey() * pageRank.get(link);
                updatedRetrievedDocuments.putIfAbsent(newScore, new HashSet<>());
                updatedRetrievedDocuments.get(newScore).add(link);
            }
        }

        return updatedRetrievedDocuments;
    }
}
