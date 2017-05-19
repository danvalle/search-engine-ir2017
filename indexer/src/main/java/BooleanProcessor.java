import java.io.*;
import java.util.*;

/**
 * Created by dan on 17/05/17.
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

            int indexNumber = Arrays.binarySearch(indexStartValues, queryId);
            if (indexNumber < 0) {
                indexNumber = -indexNumber - 2;
            }
            FileInputStream fis = new FileInputStream(indexFiles.get(indexNumber));
            BufferedReader indexReader = new BufferedReader(new InputStreamReader(fis));

            int termLine = queryId - indexStartValues[indexNumber];
            for (int i = 0; i < termLine; i++) {
                indexReader.readLine();
            }
            String line = indexReader.readLine();
            line = encoder.decodeLine(line);

            if (documentsFound.isEmpty()) {
                documentsFound = parseLineToDocuments(line);
            } else {
                documentsFound.retainAll(parseLineToDocuments(line));
            }
        }

        String[] documentsList = new String[documentsFound.size()];
        int i = 0;
        for (Integer eachDocumentFound : documentsFound) {
            documentsList[i] = document.get(eachDocumentFound);
        }

        return documentsList;
    }





}
