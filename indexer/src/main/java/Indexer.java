import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;

public class Indexer {
    private HashMap<String, Integer> vocabulary;
    private ArrayList<File> tempFiles;

    private File[] listofFiles;
    private int fileNum = 0;

    private StringBuilder html;
    private StringBuilder url;
    private HashSet<String> stopWords;

    private BufferedReader reader;
    private char[] buffer = new char[5000];
    private String[] pages = new String[0];
    private int bufferIndex = 0;

    private int docNumber = 0;

    Indexer(String dataPath) throws IOException {
        File dataFolder = new File(dataPath);
        listofFiles = dataFolder.listFiles();
        reader = getNextFile();

        vocabulary = new HashMap<>();
        tempFiles = new ArrayList<>();
        getStopWords();
    }


    public BufferedReader getNextFile() {
//        Open file with html
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(listofFiles[fileNum]);
            reader = new BufferedReader(new InputStreamReader(fis, "ISO-8859-1"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        fileNum++;
        return reader;
    }


    public int getNextPage() {
        url = new StringBuilder();
        html = new StringBuilder();
        boolean urlFound = false;
        boolean htmlFound = false;
        int endOfDocumentsFile = 0;

        while (!urlFound || !htmlFound) {
            if (bufferIndex == pages.length) {
                bufferIndex = 0;

                try {
                    endOfDocumentsFile = reader.read(buffer, 0, buffer.length);
                    if ((endOfDocumentsFile == -1) && (fileNum < listofFiles.length)) {
                        reader = getNextFile();
                        url = new StringBuilder();
                        html = new StringBuilder();
                        System.out.println("Changing file...");
                        endOfDocumentsFile = reader.read(buffer, 0, buffer.length);
                    }
                    String bufferString = new String(buffer);
                    pages = bufferString.replaceAll("\\|", "\\|\\|").split("\\|");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            Next buffer and first letter is the html_url division
            if  (pages[bufferIndex].isEmpty() && urlFound) {
                htmlFound = true;
            }

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


    public void updateVocabulary(String term) {
        if (!vocabulary.containsKey(term)) {
            vocabulary.put(term, vocabulary.size());
        }
    }


    public void getStopWords() {
        try {
            Path stopWordsFile = Paths.get(getClass().getResource("StopWords.txt").toURI());
            stopWords = new HashSet<>();
            stopWords.addAll( Files.readAllLines(stopWordsFile) );

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

    }


    public void closeDocumentsFile(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeRunIntoTempFile(SortedMap<Integer, ArrayList<Tuple>> entryList,
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


    public void buildIndex() {

//        Store tuples and frequencies
        SortedMap<Integer, ArrayList<Tuple>> entryList = new TreeMap<>();
        Map<Integer, Map<Integer, Integer>> termFrequency = new HashMap<>();
        int entriesNumber = 0;


//        Get next html and url and parse them
        while (getNextPage() != -1) {

            System.out.println(url.toString());
            Document doc = Jsoup.parse(html.toString());

//        For each term, do the trick
            String processedTolken;
            int position = 0;
            for (String tolken : doc.body().text().split(" ")) {
                processedTolken = Normalizer.normalize(tolken, Normalizer.Form.NFD);
                processedTolken = processedTolken.replaceAll("[^A-Za-z0-9]*", "").toLowerCase();
                if (StringUtil.isNumeric(processedTolken)) {
                    processedTolken = "number";
                }

                if (!stopWords.contains(processedTolken) && !processedTolken.isEmpty()) {
                    updateVocabulary(processedTolken);

                    Tuple newTuple = new Tuple(docNumber, position);
                    int tolkenNumber = vocabulary.get(processedTolken);
                    if (!entryList.containsKey(tolkenNumber)) {
                        entryList.put(tolkenNumber, new ArrayList<>());
                        termFrequency.put(tolkenNumber, new HashMap<>());
                    }
                    entryList.get(tolkenNumber).add(newTuple);

                    if (!termFrequency.get(tolkenNumber).containsKey(docNumber)) {
                        termFrequency.get(tolkenNumber).put(docNumber, 1);
                    } else {
                        int currentFrequency = termFrequency.get(tolkenNumber).get(docNumber);
                        termFrequency.get(tolkenNumber).put(docNumber, currentFrequency+1);
                    }
                    position++;

//                    Keep track of memory by looking the number of entries
                    entriesNumber++;
                    if (entriesNumber >= 50000) {
                        writeRunIntoTempFile(entryList, termFrequency);
//                        Reset entries for new run
                        entryList = new TreeMap<>();
                        termFrequency = new HashMap<>();
                        entriesNumber = 0;
                    }
                }
            }
            docNumber++;

        }

        if (entriesNumber > 0) {
            writeRunIntoTempFile(entryList, termFrequency);
        }
        closeDocumentsFile(reader);


    }

}


