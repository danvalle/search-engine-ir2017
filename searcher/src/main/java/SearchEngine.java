import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by dan on 13/06/17.
 */
public class SearchEngine {

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


    private static HashMap<Integer, Double> loadDocumentsNormFromFile() {
        HashMap<Integer, Double> documentNorm = new HashMap<>();
        try {
            File inFile = new File("./documentsNorm");
            BufferedReader docReader = new BufferedReader(new InputStreamReader((new FileInputStream(inFile))));
            String line;
            String[] splitLine;
            while ((line = docReader.readLine()) != null) {
                splitLine = line.split(" ");
                documentNorm.put(Integer.valueOf(splitLine[0]), Double.valueOf(splitLine[1]));
            }
            docReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return documentNorm;
    }


    private static void savePageRankIntoFile(HashMap<Integer, String> doc, HashMap<String, Double> pageRank) {
        try {
            File outFile = new File("./pagerank");
            BufferedWriter docWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            for (Map.Entry<Integer, String> entry : doc.entrySet()) {
                docWriter.write(entry.getKey() + " " + pageRank.get(entry.getValue())+"\n");
            }
            docWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void saveAnchorIndex(HashMap<String, Integer> anchorVocabulary,
                                        HashMap<Integer, HashSet<Integer>> anchorIndex) {


    }



    public static void main(String [] args) throws Exception {
        File indexPath = new File("index/");
        long startTime = System.currentTimeMillis();

        HashMap<String, Integer> vocabulary = loadVocabularyFromFile();
        HashMap<Integer, String> document = loadDocumentsFromFile();
        HashMap<Integer, Double> documentNorm = loadDocumentsNormFromFile();
        System.out.println("Vocabulary and Urls Loaded: " + (System.currentTimeMillis() - startTime));

//        VectorialProcessor searcher = new VectorialProcessor(indexPath.getAbsolutePath()+"/",
//                vocabulary,
//                document,
//                documentNorm);
//        SortedMap<Double, Integer> ans = searcher.search("hotel fazenda cafe");


        PageRank pagerank = new PageRank(document, "/home/dan/UFMG/RI/small_collection/");
        pagerank.getLinks();
        double alpha = 0.8;
        pagerank.iterate(alpha);

        savePageRankIntoFile(document, pagerank.pageRankValues);
        saveAnchorIndex(pagerank.anchorVocabulary, pagerank.anchorIndex);



        System.out.println("Pages found: " + (System.currentTimeMillis() - startTime));

    }
}