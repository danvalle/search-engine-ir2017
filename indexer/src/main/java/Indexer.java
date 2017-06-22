import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.*;


/**
 * Created by dan on 11/05/17.
 *
 * Indexer looks for the terms and creates temporary files to
 * be marged later
 *
 *
 */



public class Indexer {
    public HashMap<String, Integer> vocabulary;
    public HashMap<Integer, String> document;
    public HashMap<Integer, Double> documentNorm;
    private ArrayList<File> tempFiles;

    private File[] listOfFiles;
    private int fileNum = 0;

    private StringBuilder html;
    private StringBuilder url;
    private HashSet<String> stopWords;

    private BufferedReader reader;
    private char[] buffer = new char[1048576];
    private String[] pages = new String[0];
    private int bufferIndex = 0;

    private int docNumber = 0;

    Indexer(String dataPath) throws IOException {
        File dataFolder = new File(dataPath);

        listOfFiles = dataFolder.listFiles();
        reader = getNextFile();
        vocabulary = new HashMap<>();
        document = new HashMap<>();
        documentNorm = new HashMap<>();
        tempFiles = new ArrayList<>();
        getStopWords();
    }


    private BufferedReader getNextFile() {
//        Open file with html
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(listOfFiles[fileNum]);
            reader = new BufferedReader(new InputStreamReader(fis, "ISO-8859-1"));
            System.out.println("Next Page");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        fileNum++;
        return reader;
    }


    private int getNextPage() {
        url = new StringBuilder();
        html = new StringBuilder();
        boolean urlFound = false;
        boolean htmlFound = false;
        int endOfDocumentsFile = 0;

//        Looks for a new url and html reading files in blocks inside a buffer
        while (!urlFound || !htmlFound) {
            if (bufferIndex == pages.length) {
                bufferIndex = 0;

                try {
                    endOfDocumentsFile = reader.read(buffer, 0, buffer.length);
                    if ((endOfDocumentsFile == -1) && (fileNum < listOfFiles.length)) {
                        closeDocumentsFile(reader);
                        reader = getNextFile();
                        url = new StringBuilder();
                        html = new StringBuilder();
                        endOfDocumentsFile = reader.read(buffer, 0, buffer.length);
                    }
                    String bufferString = new String(buffer);
                    pages = bufferString.replaceAll("\\|", "\\|\\|").split("\\|");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            If information comes in the end of the buffer, we have to know if it continues
//            or the next block will have other information
            if  (pages[bufferIndex].isEmpty() && urlFound) {
                htmlFound = true;
            }

//            Url is the first to be found
            if (!urlFound) {
                while (pages[bufferIndex].isEmpty()) {
                    bufferIndex++;
                }
                url.append(pages[bufferIndex].replaceAll(" ", ""));
                bufferIndex++;
                if (bufferIndex!=pages.length && pages[bufferIndex].isEmpty()) {
                    urlFound = true;
                }
            }

//            When the url has already been found, go for the html
            if (urlFound && bufferIndex!=pages.length+1 && !htmlFound) {
                while (pages[bufferIndex].isEmpty()) {
                    bufferIndex++;
                }
                html.append(pages[bufferIndex]);
                bufferIndex++;
                if (bufferIndex!=pages.length && pages[bufferIndex].isEmpty()) {
                    htmlFound = true;
                }
            }
        }
        return endOfDocumentsFile;
    }


    private void updateVocabulary(String term) {
        if (!vocabulary.containsKey(term)) {
            vocabulary.put(term, vocabulary.size());
        }
    }


    private void getStopWords() {
        try {
            File stopWordsFile = new File("indexer/src/main/resources/StopWords.txt");
            stopWords = new HashSet<>();
            stopWords.addAll( Files.readAllLines(stopWordsFile.toPath()) );

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void closeDocumentsFile(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeRunIntoTempFile(SortedMap<Integer, ArrayList<Tuple>> entryList,
                                     Map<Integer, Map<Integer, Integer>> termFrequency) {

//        Write run into temporary file
        try {
            File tempFile = File.createTempFile("tempfile", ".tmp");

            BufferedWriter run = new BufferedWriter(new FileWriter(tempFile));
            for (SortedMap.Entry<Integer, ArrayList<Tuple>> entry : entryList.entrySet()) {
                int prevDoc = -1;
                int fdt = -1;
                for (Tuple ea : entry.getValue()) {
                    if (ea.d != prevDoc) {
                        fdt = termFrequency.get(entry.getKey()).get(ea.d);
                        prevDoc = ea.d;
                    }
                    run.write(entry.getKey().toString() + ' ' + String.valueOf(ea.d) +
                            ' ' + fdt + ' ' + String.valueOf(ea.p) + '\n');
                }
            }
            run.close();

            tempFiles.add(tempFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void getNorm(HashMap<Integer, Double> tfList) {
        Double squareSum = 0.0;
        Double current_tf;
        Iterator it = tfList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            current_tf = (Math.log((Double) pair.getValue()));
            squareSum += Math.pow(current_tf, 2);
        }
        documentNorm.put(docNumber, Math.sqrt(squareSum + 1));
    }


    public  ArrayList<File> buildIndex() {

//        Store tuples and frequencies
        SortedMap<Integer, ArrayList<Tuple>> entryList = new TreeMap<>();
        Map<Integer, Map<Integer, Integer>> termFrequency = new HashMap<>();
        HashMap<Integer, Double> tfList = new HashMap();
        Double current_tf;
        int entriesNumber = 0;


//        Get next html and url and parse them
        while (getNextPage() != -1) {

            Document doc = Jsoup.parse(html.toString());
            if (doc.body() == null || doc.body().text() == null || url.length() > 180 || url.length() == 0) {
                continue;
            }
            document.put(docNumber, url.toString());

//        For each term, clean it and add to the vocabulary if not seen before
            String processedTolken;
            int position = 0;
            for (String tolken : doc.body().text().split(" ")) {
                processedTolken = Normalizer.normalize(tolken, Normalizer.Form.NFD);
                processedTolken = processedTolken.replaceAll("[^A-Za-z0-9]*", "").toLowerCase();
                if (StringUtil.isNumeric(processedTolken)) {
                    processedTolken = "number";
                }

                if (!stopWords.contains(processedTolken) && !processedTolken.isEmpty()
                        && processedTolken.length() < 35) {
                    updateVocabulary(processedTolken);

//                    Create a new tuple for each occurrence of a term in a document and keep
//                    track of its frequency
                    Tuple newTuple = new Tuple(docNumber, position);
                    int tolkenNumber = vocabulary.get(processedTolken);
                    if (!entryList.containsKey(tolkenNumber)) {
                        entryList.put(tolkenNumber, new ArrayList<>());
                        termFrequency.put(tolkenNumber, new HashMap<>());
                    }
                    entryList.get(tolkenNumber).add(newTuple);
                    tfList.putIfAbsent(tolkenNumber, 0.0);
                    current_tf = tfList.get(tolkenNumber);
                    tfList.replace(tolkenNumber, current_tf+1.0);

                    if (!termFrequency.get(tolkenNumber).containsKey(docNumber)) {
                        termFrequency.get(tolkenNumber).put(docNumber, 1);
                    } else {
                        int currentFrequency = termFrequency.get(tolkenNumber).get(docNumber);
                        termFrequency.get(tolkenNumber).put(docNumber, currentFrequency+1);
                    }
                    position++;

//                    Keep track of memory by looking the number of entries
                    entriesNumber++;

                }
            }
            getNorm(tfList);
            tfList = new HashMap();
            docNumber++;

//            Reset entries for new run
            if (entriesNumber >= 500000) {
                writeRunIntoTempFile(entryList, termFrequency);
                entryList = new TreeMap<>();
                termFrequency = new HashMap<>();
                entriesNumber = 0;
            }
        }

//        Last entries that did not complete max memory defined
        if (entriesNumber > 0) {
            writeRunIntoTempFile(entryList, termFrequency);
        }
        closeDocumentsFile(reader);


        return  tempFiles;
    }
}


