import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

public class TestParser {

    @Test
    public void testShouldCreateIndex() throws Exception {
        Indexer indexer = new Indexer("/home/dan/UFMG/RI/small_collection/");
        ArrayList<File> listOfRunFiles = indexer.buildIndex();
        Merger merger = new Merger(listOfRunFiles, "/home/dan/UFMG/RI/index/out");
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
    public void testShouldSearchQuery() throws Exception {
        Indexer indexer = new Indexer("/home/dan/UFMG/RI/small_collection/");
        ArrayList<File> listOfRunFiles = indexer.buildIndex();
        Merger merger = new Merger(listOfRunFiles, "/home/dan/UFMG/RI/index/out");
        merger.mergeRuns();

        BooleanProcessor searcher = new BooleanProcessor("/home/dan/UFMG/RI/index/out",
                indexer.vocabulary,
                indexer.document);
        searcher.search("banco");
        searcher.search("aisndovnaovna");

    }


}