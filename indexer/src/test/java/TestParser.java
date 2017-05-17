import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

public class TestParser {

    @Test
    public void testShouldWork() throws Exception {
        Indexer indexer = new Indexer("/home/dan/UFMG/RI/small_collection/");
        ArrayList<File> listOfRunFiles = indexer.buildIndex();
        Merger merger = new Merger(listOfRunFiles);
        merger.mergeRuns();
    }

    @Test
    public void spamText() {
        for (int i = 0 ; i < 10 ; i++)
            System.out.println("WORKING");
    }
}