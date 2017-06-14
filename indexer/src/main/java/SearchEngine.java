import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dan on 18/05/17.
 *
 * Choose between create an index or search for documents in
 * an index previously created
 *
 */
public class SearchEngine {

    private static void saveVocabularyIntoFile(HashMap<String, Integer> voc) {
        try {
            File outFile = new File("./vocabulary");
            BufferedWriter vocWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            for (Map.Entry<String, Integer> entry : voc.entrySet()) {
                vocWriter.write(entry.getKey() + ";" + entry.getValue()+"\n");
            }
            vocWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static HashMap<String, Integer> loadVocabularyFromFile() {
        HashMap<String, Integer> vocabulary = new HashMap<>();
        try {
            File inFile = new File("./vocabulary");
            BufferedReader vocReader = new BufferedReader(new InputStreamReader((new FileInputStream(inFile))));
            String line;
            String[] splitLine;
            while ((line = vocReader.readLine()) != null) {
                splitLine = line.split(";");
                vocabulary.put(splitLine[0], Integer.valueOf(splitLine[1]));
            }
            vocReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return vocabulary;
    }


    private static void saveDocumentsIntoFile(HashMap<Integer, String> doc, HashMap<Integer, Double> docNorm) {
        try {
            File outFile = new File("./documents");
            File normFile = new File("./documentsNorm");
            BufferedWriter docWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            BufferedWriter normWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(normFile)));
            for (Map.Entry<Integer, String> entry : doc.entrySet()) {
                docWriter.write(entry.getKey() + " " + entry.getValue()+"\n");
                normWriter.write(entry.getKey() + " " + docNorm.get(entry.getKey()) +"\n");
            }
            docWriter.close();
            normWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static HashMap<Integer, String> loadDocumentsFromFile() {
        HashMap<Integer, String> document = new HashMap<>();
        try {
            File inFile = new File("./documents");
            BufferedReader docReader = new BufferedReader(new InputStreamReader((new FileInputStream(inFile))));
            String line;
            String[] splitLine;
            while ((line = docReader.readLine()) != null) {
                splitLine = line.split(" ");
                document.put(Integer.valueOf(splitLine[0]), splitLine[1]);
            }
            docReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }


    public static void main(String [] args) throws Exception {
        File indexPath = new File("index/");
        long startTime = System.currentTimeMillis();

//        If argument is index, create a new index
        if (args[0].equals("index")) {
//            First we get the input and create the temporary files
            Indexer indexer = new Indexer(args[1]);
            ArrayList<File> listOfRunFiles = indexer.buildIndex();
            System.out.println("Runs Created: " + (System.currentTimeMillis() - startTime));
            System.out.println("Vocabulary Size: " + indexer.vocabulary.size());
            System.out.println("Documents Size: " + indexer.document.size());
//            Then, we merge the temporary files correctly into the index file
            Merger merger = new Merger(listOfRunFiles, indexPath.getAbsolutePath()+"/");
            merger.mergeRuns();
            System.out.println("Index Created: " + (System.currentTimeMillis() - startTime));

            saveVocabularyIntoFile(indexer.vocabulary);
            saveDocumentsIntoFile(indexer.document, indexer.documentNorm);
            System.out.println("Vocabulary and Urls Saved: " + (System.currentTimeMillis() - startTime));

//         If the argument is search, get index already existent and search in it
        } else if (args[0].equals("search")) {
            HashMap<String, Integer> vocabulary = loadVocabularyFromFile();
            HashMap<Integer, String> document = loadDocumentsFromFile();
            System.out.println("Vocabulary and Urls Loaded: " + (System.currentTimeMillis() - startTime));

            BooleanProcessor searcher = new BooleanProcessor(indexPath.getAbsolutePath()+"/",
                    vocabulary,
                    document);

            String[] ans = searcher.search(args[1]);
            System.out.println("Pages found: " + (System.currentTimeMillis() - startTime));
            for(String url : ans) {
                System.out.println(url);
            }

        } else {
            System.out.println("Error: Choose between index and search");
        }

    }
}
