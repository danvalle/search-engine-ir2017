import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by dan on 14/06/17.
 */
public class PageRank {
    public HashMap<String, HashSet<String>> pointedLinks;
    public HashMap<String, Double> pageRankValues;
    public HashMap<Integer, String> document;
    public HashSet<String> documentSet;
    public HashMap<String, Integer> linksNum;

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


    private void parseLinks(Document doc) {
        Elements elements = doc.select("a");
        String link;

        for (Element element : elements) {
            link = element.absUrl("href");
            if (!link.isEmpty() && !link.equals(url.toString()) && documentSet.contains(link)) {
                if (!pointedLinks.containsKey(link)) {
                    pointedLinks.put(link, new HashSet<>());
                }
                pointedLinks.get(link).add(url.toString());
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


    public void iterate() {

        Double currentPageRank;
        Iterator it = document.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry u = (Map.Entry)it.next();
            currentPageRank = 0.0;

            if (!pointedLinks.containsKey(u.getValue())) {
                System.out.println("No tein");
                continue;
            }

            for (String v : pointedLinks.get(u.getValue())) {
                currentPageRank += pageRankValues.get(v) / linksNum.get(v);

            }

            pageRankValues.put((String) u.getValue(), currentPageRank);

        }






    }










}
