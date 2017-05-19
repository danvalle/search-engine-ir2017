import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

public class TestParser {

    @Test
    public void testShouldCreateIndex() throws Exception {
        Indexer indexer = new Indexer("/home/dan/UFMG/RI/small_collection/");
        ArrayList<File> listOfRunFiles = indexer.buildIndex();
        Merger merger = new Merger(listOfRunFiles, "/home/dan/UFMG/RI/index/");
        merger.mergeRuns();
    }


    @Test
    public void testShouldEncodeAndDecode() {
        Encoder encoder = new Encoder();
        for (int i=0; i < 100000; i++) {
            String encoded = encoder.encode(i);
            System.out.println(i +" -> "+ String.valueOf(encoded));

            int decoded = encoder.decode(encoded);
            assert(i == decoded);
        }
    }


    @Test
    public void testShouldDecodeLine() {
        Encoder encoder = new Encoder();
        String encoded = "";

        encoded += encoder.encode(0) +
                encoder.encode(10) +
                encoder.encode(100) +
                encoder.encode(1000) +
                encoder.encode(10000);

        String result = encoder.decodeLine(encoded);
        assert (result.equals("0 10 100 1000 10000 "));
    }


    @Test
    public void testShouldSearchQuery() throws Exception {
        Indexer indexer = new Indexer("/home/dan/UFMG/RI/small_collection/");
        ArrayList<File> listOfRunFiles = indexer.buildIndex();
        Merger merger = new Merger(listOfRunFiles, "/home/dan/UFMG/RI/index/");
        merger.mergeRuns();

        BooleanProcessor searcher = new BooleanProcessor("/home/dan/UFMG/RI/index/",
                indexer.vocabulary,
                indexer.document);
        String[] ans = searcher.search("dinheiro and banco and ganhar");

        for(String url : ans) {
            System.out.println(url);
        }
    }


}