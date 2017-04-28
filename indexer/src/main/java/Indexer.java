import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Indexer {
    private HashMap<String, Integer> vocabulary;
    private File temp;

    private String html;
    private String url;

    Indexer() throws IOException {
        vocabulary = new HashMap<>();
        temp = File.createTempFile("tempfile", ".tmp");
    }


    public void getNextPage() {
        url = "http://www.ohyeah.com.br";
        html = "<html><head><title>First parse</title></head>"
                + "<body><p>Parsed HTML into a doc. This doc is really important because" +
                " it is going to be a indexed doc.</p></body></html>";
    }

    public void updateVocabulary(String term) {
        if (!vocabulary.containsKey(term)) {
            vocabulary.put(term, vocabulary.size());
        }
    }


    public void buildIndex() {
//        OPEN FILES
        SortedMap<Integer, ArrayList<Tuple>> entryList = new TreeMap<>();


        getNextPage();
        Document doc = Jsoup.parse(html);

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
                System.out.println(String.valueOf(ea.d) + ' ' + String.valueOf(ea.p));
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


