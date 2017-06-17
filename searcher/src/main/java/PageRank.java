import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.*;

/**
 * Created by dan on 14/06/17.
 */
public class PageRank {
    public HashMap<String, HashSet<String>> pointedLinks;
    public HashMap<String, Double> pageRankValues;
    public HashMap<Integer, String> document;
    public HashSet<String> documentSet;
    public HashMap<String, Integer> linksNum;

    public HashMap<String, Integer> anchorVocabulary;
    public HashMap<String, Integer> anchorDocument;
    public HashMap<Integer, HashSet<Integer>> anchorIndex;
    private HashSet<String> stopWords;

    private Double topRank;

    private File[] listOfFiles;
    private int fileNum = 0;

    private StringBuilder html;
    private StringBuilder url;

    private BufferedReader reader;
    private char[] buffer = new char[1048576];
    private String[] pages = new String[0];
    private int bufferIndex = 0;

    PageRank(HashMap<Integer, String> document, String dataPath){
        this.document = document;
        pointedLinks = new HashMap<>();
        pageRankValues = new HashMap<>();
        documentSet = new HashSet<>();
        linksNum = new HashMap<>();

        anchorVocabulary = new HashMap<>();
        anchorIndex = new HashMap<>();
        anchorDocument = new HashMap<>();
        getStopWords();

        Iterator it = this.document.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            documentSet.add(pair.getValue().toString());
            pageRankValues.put((String) pair.getValue(), 1.0);
            linksNum.put((String) pair.getValue(), 0);
        }

        File dataFolder = new File(dataPath);
        listOfFiles = dataFolder.listFiles();
        reader = getNextFile();

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

    private BufferedReader getNextFile() {
//        Open file with html
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(listOfFiles[fileNum]);
            reader = new BufferedReader(new InputStreamReader(fis, "ISO-8859-1"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        fileNum++;
        return reader;
    }


    private void closeDocumentsFile(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    private void handleAnchor(String anchor, String link) {
        String processedTolken;
        int tokenId;
        for (String tolken : anchor.split(" ")) {
            processedTolken = Normalizer.normalize(tolken, Normalizer.Form.NFD);
            processedTolken = processedTolken.replaceAll("[^A-Za-z0-9]*", "").toLowerCase();
            if (StringUtil.isNumeric(processedTolken)) {
                processedTolken = "number";
            }
            if (!stopWords.contains(processedTolken) && processedTolken.length() > 1
                    && processedTolken.length() < 35) {

                anchorVocabulary.putIfAbsent(processedTolken, anchorVocabulary.size());
                anchorDocument.putIfAbsent(link, anchorDocument.size());

                tokenId = anchorVocabulary.get(processedTolken);
                anchorIndex.putIfAbsent(tokenId, new HashSet<>());
                anchorIndex.get(tokenId).add(anchorDocument.get(link));
            }
        }
    }


    private void parseLinks(Document doc) {
        Elements elements = doc.select("a");
        String link;
        String anchor;

        for (Element element : elements) {
            link = element.absUrl("href");
            if (!link.isEmpty() && !link.equals(url.toString())) {
                if (documentSet.contains(link)) {
                    if (!pointedLinks.containsKey(link)) {
                        pointedLinks.put(link, new HashSet<>());
                    }
                    pointedLinks.get(link).add(url.toString());

                    anchor = element.text();
                    if (!anchor.isEmpty()) {
                        handleAnchor(anchor, link);
                    }

                }
                linksNum.replace(url.toString(), linksNum.get(url.toString())+1);
            }
        }
    }


    public void getLinks() {
        while (getNextPage() != -1) {

            Document doc = Jsoup.parse(html.toString());
            if (doc.body() == null || doc.body().text() == null || url.length() > 180 || url.length() == 0) {
                continue;
            }

            parseLinks(doc);
        }
    }


    private HashMap<String, Double> createCurrentPageRankValues() {
        HashMap<String, Double> currentPageRank = new HashMap<>();
        for (String documentName : document.values()) {
            currentPageRank.put(documentName, 0.0);
        }
        return currentPageRank;
    }


    private Double updatePageRankValues(HashMap<String, Double> currentPageRankValues) {
        Double dif = 0.0;
        Double currentValue;
        topRank = 0.0;
        for (String key : pageRankValues.keySet()) {
            currentValue = currentPageRankValues.get(key);
            if (currentValue > topRank) {
                topRank = currentValue;
            }
            dif += Math.abs(currentValue - pageRankValues.get(key));
            pageRankValues.replace(key, currentValue);
        }

        return dif;
    }


    public void iterate(double alpha) {
        Double currentPageRank;
        Double pageRankDif = 1.0;
        HashMap<String, Double> currentPageRankValues;

        while (pageRankDif > alpha*0.01) {
            currentPageRankValues = createCurrentPageRankValues();
            Iterator it = document.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry u = (Map.Entry) it.next();
                currentPageRank = 0.0;
                if (!pointedLinks.containsKey(u.getValue())) {
                    continue;
                }

                for (String v : pointedLinks.get(u.getValue())) {
                    currentPageRank += (alpha * pageRankValues.get(v)) / linksNum.get(v);

                }

                currentPageRankValues.put((String) u.getValue(), currentPageRank);
            }

            pageRankDif = updatePageRankValues(currentPageRankValues);
        }
    }


    public void normalize() {
        Double normalizedValue;
        Iterator it = pageRankValues.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            normalizedValue = (Double) pair.getValue() / topRank;
            pageRankValues.replace((String) pair.getKey(), normalizedValue + 1);
        }
    }
}
