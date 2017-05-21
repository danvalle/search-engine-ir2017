import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by dan on 17/05/17.
 *
 * Search for the terms in the index and work as a set
 *
 */
public class BooleanProcessor {
    private int[] indexStartValues;
    private SortedMap<Integer, File> indexFiles;
    private HashMap<String, Integer> vocabulary;
    private HashMap<Integer, String> document;


    BooleanProcessor(String indexFileFolder,
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

//    Gets the documents related to the term in the read line
    private Set<Integer> parseLineToDocuments(String line) {
        Set<Integer> docsInLine = new HashSet<>();
        String[] splitLine = line.split(" ");
        int NumberOfDocs = Integer.valueOf(splitLine[0]);
        int docSum = 0;

        int i = 1;
        while (docsInLine.size() != NumberOfDocs) {
            docSum += Integer.valueOf(splitLine[i]);
            docsInLine.add(docSum);

            i += (Integer.valueOf(splitLine[i+1]) + 2);
        }


        return docsInLine;
    }


//    Looks for the term in the index and creates a set
    public String[] search(String query) throws Exception {
        String[] queryTerms = query.split("and");
        Encoder encoder = new Encoder();
        Set<Integer> documentsFound = new HashSet<>();

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
//            Work as a set to get only the intersection
            if (documentsFound.isEmpty()) {
                documentsFound = parseLineToDocuments(line);
            } else {
                documentsFound.retainAll(parseLineToDocuments(line));
            }
        }
//        Get the names of the documents found
        String[] documentsList = new String[documentsFound.size()];
        int i = 0;
        for (Integer eachDocumentFound : documentsFound) {
            documentsList[i] = document.get(eachDocumentFound);
            i++;
        }

        return documentsList;
    }

}
