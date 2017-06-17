import javax.annotation.processing.SupportedSourceVersion;
import java.io.*;
import java.util.*;

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


    private static HashMap<Integer, Double> loadPageRankFromFile() {
        HashMap<Integer, Double> pageRank = new HashMap<>();
        try {
            File inFile = new File("./pagerank");
            BufferedReader docReader = new BufferedReader(new InputStreamReader((new FileInputStream(inFile))));
            String line;
            String[] splitLine;
            while ((line = docReader.readLine()) != null) {
                splitLine = line.split(" ");
                pageRank.put(Integer.valueOf(splitLine[0]), Double.valueOf(splitLine[1]));
            }
            docReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pageRank;
    }


    private static void saveAnchorIndex(HashMap<Integer, HashSet<Integer>> anchorIndex) {
        Encoder encoder = new Encoder();
        try {
            File outFile = new File("./anchorIndex");
            BufferedWriter docWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            for (Map.Entry<Integer, HashSet<Integer>> entry : anchorIndex.entrySet()) {
                docWriter.write(encoder.encode(entry.getKey()));
                for (Integer docNumber : entry.getValue()) {
                    docWriter.write(" " + encoder.encode(docNumber));
                }
                docWriter.write("\n");
            }
            docWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static HashMap<Integer, HashSet<Integer>> loadAnchorIndex() {
        HashMap<Integer, HashSet<Integer>> anchorIndex = new HashMap<>();
        Encoder decoder = new Encoder();
        try {
            File inFile = new File("./anchorIndex");
            BufferedReader docReader = new BufferedReader(new InputStreamReader((new FileInputStream(inFile))));
            String line;
            String[] splitLine;
            while ((line = docReader.readLine()) != null) {
                splitLine = decoder.decodeLine(line).split(" ");
                anchorIndex.put(Integer.valueOf(splitLine[0]), new HashSet<>());
                for (String eachDocument : splitLine) {
                    anchorIndex.get(Integer.valueOf(splitLine[0])).add(Integer.valueOf(eachDocument));
                }
            }
            docReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return anchorIndex;
    }


    private static void saveAnchorVocabulary(HashMap<String, Integer> anchorVocabulary) {
        try {
            File outFile = new File("./anchorVocabulary");
            BufferedWriter vocWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            for (Map.Entry<String, Integer> entry : anchorVocabulary.entrySet()) {
                vocWriter.write(entry.getKey() + ";" + entry.getValue() + "\n");
            }
            vocWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static HashMap<String, Integer> loadAnchorVocabulary() {
        HashMap<String, Integer> anchorVocabulary = new HashMap<>();
        try {
            File inFile = new File("./anchorVocabulary");
            BufferedReader vocReader = new BufferedReader(new InputStreamReader((new FileInputStream(inFile))));
            String line;
            String[] splitLine;
            while ((line = vocReader.readLine()) != null) {
                splitLine = line.split(";");
                anchorVocabulary.put(splitLine[0], Integer.valueOf(splitLine[1]));
            }
            vocReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return anchorVocabulary;
    }


    private static void saveAnchorDocument(HashMap<String, Integer> anchorDocument) {
        try {
            File outFile = new File("./anchorDocuments");
            BufferedWriter docWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            for (Map.Entry<String, Integer> entry : anchorDocument.entrySet()) {
                docWriter.write(entry.getValue() + " " + entry.getKey() + "\n");
            }
            docWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static HashMap<Integer, String> loadAnchorDocument() {
        HashMap<Integer, String> anchorDocument = new HashMap<>();
        try {
            File inFile = new File("./anchorDocuments");
            BufferedReader docReader = new BufferedReader(new InputStreamReader((new FileInputStream(inFile))));
            String line;
            String[] splitLine;
            while ((line = docReader.readLine()) != null) {
                splitLine = line.split(" ");
                anchorDocument.put(Integer.valueOf(splitLine[0]), splitLine[1]);
            }
            docReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return anchorDocument;
    }


    public static void main(String [] args) throws Exception {
        File indexPath = new File("index/");
        long startTime = System.currentTimeMillis();

        HashMap<Integer, String> document = loadDocumentsFromFile();
        System.out.println("Loaded: " + (System.currentTimeMillis() - startTime));

        String query = "hotel fazenda cafe";
        if (args[0].equals("pagerank")) {
            Double alpha = Double.valueOf(args[1]);

            PageRank pagerank = new PageRank(document, "/home/dan/UFMG/RI/small_collection/");
            pagerank.getLinks();
            pagerank.iterate(alpha);
            pagerank.normalize();

            savePageRankIntoFile(document, pagerank.pageRankValues);
            saveAnchorIndex(pagerank.anchorIndex);
            saveAnchorVocabulary(pagerank.anchorVocabulary);
            saveAnchorDocument(pagerank.anchorDocument);

        } else if (args[0].equals("search")) {
            HashMap<String, Integer> vocabulary = loadVocabularyFromFile();
            HashMap<Integer, Double> documentNorm = loadDocumentsNormFromFile();

            VectorialProcessor searcher = new VectorialProcessor(indexPath.getAbsolutePath() + "/",
                    vocabulary,
                    documentNorm);
            SortedMap<Double, HashSet<Integer>> ans = searcher.search(query);

            if (args[1].equals("1") || args[1].equals("3")) {
                HashMap<Integer, Double> pageRank = loadPageRankFromFile();
                PageRankProcessor pageRankProcessor = new PageRankProcessor(pageRank);
                ans = pageRankProcessor.updateRetrievedDocuments(ans);
            }

            if (args[1].equals("2") || args[1].equals("3")) {
                HashMap<Integer, HashSet<Integer>> anchorIndex = loadAnchorIndex();
                HashMap<String, Integer> anchorVocabulary = loadAnchorVocabulary();
                HashMap<Integer, String> anchorDocument = loadAnchorDocument();
                AnchorProcessor anchorProcessor = new AnchorProcessor(anchorIndex, anchorVocabulary, anchorDocument);
                ans = anchorProcessor.updateRetrievedDocuments(query, ans, document);
            }

            System.out.println("Pages found: " + (System.currentTimeMillis() - startTime));

            Iterator it = ans.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                for (Integer docId : (HashSet<Integer>) pair.getValue()) {
                    System.out.println(pair.getKey() + " - " + document.get(docId));
                }
            }

        } else {
            System.out.println("ERROR: Command not found");

        }
    }
}