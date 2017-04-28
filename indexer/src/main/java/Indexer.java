import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.*;

public class Indexer {
    private HashMap<String, Integer> vocabulary;
    private File temp;

    private StringBuilder html;
    private String url;

    Indexer() throws IOException {
        vocabulary = new HashMap<>();
        temp = File.createTempFile("tempfile", ".tmp");
    }


    public void getNextPage(Scanner scan) {
        if (scan.hasNext()) {
            url = scan.next();
            scan.next();
        }

        String nextWord;
        html = new StringBuilder();
        while (scan.hasNext()) {
            nextWord = scan.next();
            if (nextWord.contains("|||")) {
                break;
            }
            html.append(nextWord);
        }
    }

    public void updateVocabulary(String term) {
        if (!vocabulary.containsKey(term)) {
            vocabulary.put(term, vocabulary.size());
        }
    }


    public void buildIndex() {
        SortedMap<Integer, ArrayList<Tuple>> entryList = new TreeMap<>();

//        Open file with html
        File file;
        Scanner scan = null;
        try {
            file = new File("/home/dan/UFMG/RI/data_backup/html_first/html_0.txt");
            scan = new Scanner(new BufferedReader(new FileReader(file)));
            scan.next();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        Get next html and url and parse them
        getNextPage(scan);
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


