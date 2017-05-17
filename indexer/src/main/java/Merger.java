import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dan on 16/05/17.
 */
public class Merger {
    private ArrayList<BufferedReader> runsList;

    Merger(ArrayList<File> listOfFiles) {
        runsList = new ArrayList<>();
        createRunReaders(listOfFiles);
    }


    private void writeIndexLine(int currentTerm, ArrayList<String> indexLine) {
        for(String aaa : indexLine) {
            System.out.println(aaa);
        }
    }


    private void createRunReaders(ArrayList<File> listOfFiles) {
        for(File run : listOfFiles) {
            try {
                runsList.add(new BufferedReader(new FileReader(run)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void mergeRuns() throws IOException {
        ArrayList<String> indexLine = new ArrayList<>();

        HashMap<Integer, String> next = new HashMap<>(runsList.size());
        ArrayList<Integer> endedFiles = new ArrayList<>(runsList.size());
        for(int i = 0; i < runsList.size(); i++) {
            next.put(i, "");
            endedFiles.add(i, 0);
        }

        int current_term = 0;
        int filesHasEnded = 0;
        while (filesHasEnded != runsList.size()) {
            int runNumber = 0;
            for (BufferedReader runReader : runsList) {
                boolean termEnded = false;
                while (!termEnded) {
                    String line;
                    if(next.get(runNumber).isEmpty()) {
                        line = runReader.readLine();
                    } else {
                        line = next.get(runNumber);
                        next.put(runNumber, "");

                    }

                    if (line == null) {
                        if (endedFiles.get(runNumber) == 0) {
                            endedFiles.set(runNumber, 1);
                            filesHasEnded++;
                        }
                        termEnded = true;
                    }
                    else if (Integer.valueOf(line.split(" ")[0]) == current_term) {
                        indexLine.add(line);

                    } else {
                        next.put(runNumber, line);
                        termEnded = true;
                    }

                }
                runNumber++;
            }

            writeIndexLine(current_term, indexLine);
            indexLine = new ArrayList<>();
            current_term++;
        }
    }
}
