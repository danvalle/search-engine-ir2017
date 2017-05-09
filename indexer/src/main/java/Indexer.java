import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.*;

public class Indexer {
    private HashMap<String, Integer> vocabulary;
    private File temp;

    private StringBuilder html;
    private StringBuilder url;

    byte[] buff = new byte[1024];


    Indexer() throws IOException {
        vocabulary = new HashMap<>();
        temp = File.createTempFile("tempfile", ".tmp");
    }


    public void getNextPage(InputStream in) {
        int bytesRead;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            bytesRead = in.read(buff);
            out.write(buff, 0, bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = out.toByteArray();
        ByteArrayInputStream bin = new ByteArrayInputStream(data);

        byte[] bars = new byte[3];
        bin.read(bars,0,3);
        int nextByte;
        url = new StringBuilder();
        while ((nextByte = bin.read()) != -1) {
            if ((char)nextByte == '|') {
                break;
            }
            if ((char)nextByte != ' ') {
                url.append((char) nextByte);
            }
        }

        html = new StringBuilder();
        while ((nextByte = bin.read()) != -1) {
            if ((char)nextByte == '|') {
                break;
            }
            html.append((char) nextByte);
        }

        nextByte = 0;
//        if (scan.hasNext()) {
//            url = scan.next();
//            scan.next();
//        }
//
//        String nextWord;
//        html = new StringBuilder();
//        while (scan.hasNext()) {
//            nextWord = scan.next();
//            if (nextWord.contains("|||")) {
//                break;
//            }
//            html.append(nextWord);
//        }
    }

    public void updateVocabulary(String term) {
        if (!vocabulary.containsKey(term)) {
            vocabulary.put(term, vocabulary.size());
        }
    }


    public void buildIndex() {
        SortedMap<Integer, ArrayList<Tuple>> entryList = new TreeMap<>();

//        Open file with html
        InputStream in = null;
        try {

            File file = new File("/home/dan/UFMG/RI/data_backup/html_first/html_0.txt");
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


//        Get next html and url and parse them
        getNextPage(in);
        Document doc = Jsoup.parse(html.toString());

//        For each term, do the trick
        String processedTolken;
        int position = 0;
        for (String tolken : doc.body().text().split(" ")) {
            processedTolken = tolken.replaceAll("[^A-Za-z]*", "").toLowerCase();
            updateVocabulary(processedTolken);


            Tuple newTuple = new Tuple(0, position);
            if (!entryList.containsKey(vocabulary.get(processedTolken))) {
                entryList.put(vocabulary.get(processedTolken), new ArrayList<>());
            }
            entryList.get(vocabulary.get(processedTolken)).add(newTuple);


            position ++;
        }





        System.out.println(vocabulary);
        for (SortedMap.Entry<Integer, ArrayList<Tuple>> entry : entryList.entrySet()) {
            System.out.println(entry.getKey() + " =>  " + entry.getValue());
            for (Tuple ea : entry.getValue()) {
                System.out.println('(' + String.valueOf(ea.d) + " , " + String.valueOf(ea.p) + ')');
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


