import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dan on 16/05/17.
 */
public class Merger {
    private ArrayList<BufferedReader> runsList;
    private BufferedWriter indexWriter;
    private Encoder enconder;

    Merger(ArrayList<File> listOfFiles, String outFileName) {
        runsList = new ArrayList<>();
        createRunReaders(listOfFiles);

        indexWriter = createOutFile(outFileName);
        enconder = new Encoder();
    }


    private BufferedWriter createOutFile(String outFileName) {
        BufferedWriter bw = null;
        try {
            File outFile = new File(outFileName);
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bw;
    }


    private void writeIndexLine(String indexLineFormat) {
        try {
            indexWriter.write(indexLineFormat);
            System.out.print(indexLineFormat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void createIndexLine(ArrayList<String> indexLine) {
        StringBuilder indexLineFormat = new StringBuilder();
        int NumberOfDocs = 0;
        int previousDoc = -1;
        int previousPosition = 0;
        for(String line : indexLine) {
            String[] lineSplit = line.split(" ");
            if (Integer.valueOf(lineSplit[1]) != previousDoc) {
                int gapDoc;
                if (previousDoc != -1) {
                    indexLineFormat.append("),");
                    gapDoc = Integer.valueOf(lineSplit[1]) - previousDoc;
                } else {
                    gapDoc = Integer.valueOf(lineSplit[1]);
                }

                indexLineFormat.append("("+gapDoc+";"+lineSplit[3]);
                previousDoc = Integer.valueOf(lineSplit[1]);
                previousPosition = Integer.valueOf(lineSplit[3]);
                NumberOfDocs++;

            } else {
                int gapPos = Integer.valueOf(lineSplit[3]) - previousPosition;
                indexLineFormat.append(","+gapPos);
                previousPosition = Integer.valueOf(lineSplit[3]);
            }
        }
        indexLineFormat.append(")]\n");

        writeIndexLine("["+NumberOfDocs+";"+indexLineFormat.toString());
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

            createIndexLine(indexLine);
            indexLine = new ArrayList<>();
            current_term++;
        }
    }
}
