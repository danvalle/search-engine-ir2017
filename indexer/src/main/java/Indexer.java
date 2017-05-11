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
import java.util.stream.Collectors;

public class Indexer {
    private HashMap<String, Integer> vocabulary;
    private File temp;

    private StringBuilder html;
    private StringBuilder url;
    private HashSet<String> stopWords;

    char[] buffer = new char[5000];
    String[] pages = new String[1];
    int bufferIndex = 0;


    Indexer() throws IOException {
        vocabulary = new HashMap<>();
        temp = File.createTempFile("tempfile", ".tmp");

        getStopWords();
    }


//  TODO -- HANDLE EOF TO HTML
    public void getNextPage(BufferedReader reader) {
        url = new StringBuilder();
        html = new StringBuilder();
        boolean urlFound = false;
        boolean htmlFound = false;

        while (!urlFound || !htmlFound) {
            if (bufferIndex+1 == pages.length) {
                bufferIndex = 0;

                try {
                    reader.read(buffer, 0, buffer.length);
                    String bufferString = new String(buffer);
                    pages = bufferString.split("\\|");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            while (pages[bufferIndex].isEmpty()) {
                bufferIndex++;
            }

            if (!urlFound) {
                url.append(pages[bufferIndex].replaceAll(" ", ""));
                if (pages.length != bufferIndex+1) {
                    urlFound = true;
                    bufferIndex++;
                }
            }

            if (urlFound) {
                html.append(pages[bufferIndex]);
                if (pages.length != bufferIndex+1) {
                    htmlFound = true;
                    bufferIndex++;
                }
            }
        }
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


    public void buildIndex() {

//        Open file with html
        File file;
        FileInputStream fis;
        String encoding ="ISO-8859-1";
        BufferedReader reader = null;
        try {
            file = new File("/home/dan/UFMG/RI/small_collection/html_0");
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, encoding));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        Store tuples and frequencies
        SortedMap<Integer, ArrayList<Tuple>> entryList = new TreeMap<>();
        Map<Integer, Map<Integer, Integer>> termFrequency = new HashMap<>();


        for (int docNumber = 0; docNumber < 10; docNumber++) {

//        Get next html and url and parse them
            getNextPage(reader);
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
                }
            }



        }




//        PRINT SANITY
        System.out.println(vocabulary);
        for (SortedMap.Entry<Integer, ArrayList<Tuple>> entry : entryList.entrySet()) {
            System.out.println(entry.getKey() + " =>  " + vocabulary.entrySet()
                                                        .stream()
                                                        .filter(any -> Objects.equals(any.getValue(), entry.getKey()))
                                                        .map(Map.Entry::getKey)
                                                        .collect(Collectors.toSet()));
            int prevDoc = -1;
            int fdt = -1;
            for (Tuple ea : entry.getValue()) {
                if (ea.d != prevDoc) {
                    fdt = termFrequency.get(entry.getKey()).get(ea.d);
                    prevDoc = ea.d;
                }
                System.out.println('(' + String.valueOf(ea.d) + " , " + fdt + ", " + String.valueOf(ea.p) + ')');
            }
        }



//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
//            bw.write("<1, 2, 3>");
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



    }

}


