import org.junit.Test;

public class TestParser {

    @Test
    public void testShouldWork() throws Exception {
        Indexer indexer = new Indexer("/home/dan/UFMG/RI/small_collection/");
        indexer.buildIndex();
    }

    @Test
    public void spamText() {
        for (int i = 0 ; i < 10 ; i++)
            System.out.println("WORKING");
    }
}