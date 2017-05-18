import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * Created by dan on 17/05/17.
 */
public class BooleanProcessor {
    private RandomAccessFile index;
    private HashMap<String, Integer> vocabulary;
    private HashMap<Integer, String> document;

    BooleanProcessor(String indexFileName,
                     HashMap<String, Integer> vocabulary,
                     HashMap<Integer, String> document) {
        this.vocabulary = vocabulary;
        this.document = document;
        index = openIndex(indexFileName);
    }


    private RandomAccessFile openIndex(String indexFileName) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(new File(indexFileName), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return raf;
    }







    public void search(String query) {
        System.out.println(query + " - " + vocabulary.get(query));
        if (vocabulary.get(query) == null) {
            System.out.print("It does not exist");
        }

    }


}
