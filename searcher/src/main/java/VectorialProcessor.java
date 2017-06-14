import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by dan on 13/06/17.
 */
public class VectorialProcessor {
    private int[] indexStartValues;
    private SortedMap<Integer, File> indexFiles;
    private HashMap<String, Integer> vocabulary;
    private HashMap<Integer, String> document;


    VectorialProcessor(String indexFileFolder,
                     HashMap<String, Integer> vocabulary,
                     HashMap<Integer, String> document) {
        this.vocabulary = vocabulary;
        this.document = document;

        File[] filesList = new File(indexFileFolder).listFiles();
        indexFiles = new TreeMap<>();
        indexStartValues = new int[filesList.length];
        int i = 0;
        for (File eachFile : filesList) {
            String filePath = eachFile.getPath();
            indexStartValues[i] = Integer.valueOf(filePath.substring(filePath.lastIndexOf('/')+1));

            indexFiles.put(indexStartValues[i], eachFile);
            i++;
        }
    }

    //    Update values used in cosine distance
    private void parseTermLine(String line, Map<Integer, Double> documentsDist) {
        String[] splitLine = line.split(" ");
        Integer NumberOfDocs = Integer.valueOf(splitLine[0]);
        Integer docSum = 0;

        Double idf = Math.log(vocabulary.size() / NumberOfDocs);
        Double current_tf;
        Double current_tf_idf;
        Integer i = 1;
        while (i < splitLine.length) {
            docSum += Integer.valueOf(splitLine[i]);
            documentsDist.putIfAbsent(docSum, 0.0);

            current_tf = 1 + Math.log(Integer.valueOf(splitLine[i+1]));
            current_tf_idf = documentsDist.get(docSum) + (current_tf * idf);
            documentsDist.replace(docSum, current_tf_idf);

            i += (Integer.valueOf(splitLine[i+1]) + 2);
        }
    }


    //    Looks for the term in the index and creates a set
    public String[] search(String query) throws Exception {
        String[] queryTerms = query.split("and");
        Encoder encoder = new Encoder();
        Map<Integer, Double> documentsDist = new HashMap<>();

        for (String queryTerm : queryTerms) {
            queryTerm = queryTerm.replaceAll(" ", "");
            int queryId = vocabulary.get(queryTerm);
            if (vocabulary.get(queryTerm) == null) {
                throw new Exception("ERROR: Term " + queryTerm + " does not exist.");
            }
//            Finds which file contains the term
            Arrays.sort(indexStartValues);
            int indexNumber = Arrays.binarySearch(indexStartValues, queryId);
            if (indexNumber < 0) {
                indexNumber = -indexNumber - 2;
            }

            FileInputStream fis = new FileInputStream(indexFiles.get(indexStartValues[indexNumber]));
            BufferedReader indexReader = new BufferedReader(new InputStreamReader(fis));

            int termLine = queryId - indexStartValues[indexNumber];
            for (int i = 0; i < termLine; i++) {
                indexReader.readLine();
            }

            String line = indexReader.readLine();
            line = encoder.decodeLine(line);
            parseTermLine(line, documentsDist);
        }



        String[] aaa = {"Be","Good"};
        return aaa;
    }

}
