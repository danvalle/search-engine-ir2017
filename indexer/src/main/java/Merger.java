import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dan on 16/05/17.
 */
public class Merger {
    private ArrayList<BufferedReader> runsList;
    private BufferedWriter indexWriter;
    private Encoder encoder;

    private String outFileFolder;
    private int maxFileSize = 30000;
    private int NumberOfFiles = 1;


    Merger(ArrayList<File> listOfFiles, String outFileFolder) {
        runsList = new ArrayList<>();
        createRunReaders(listOfFiles);

        this.outFileFolder = outFileFolder;
        indexWriter = createOutFile();
        encoder = new Encoder();
    }


    private BufferedWriter createOutFile() {
        BufferedWriter bw = null;
        try {
            File outFile = new File(outFileFolder+'0');
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bw;
    }


    private void writeIndexLine(String indexLineFormat, int currentTerm) {
        try {
            if (currentTerm == maxFileSize*NumberOfFiles) {
                indexWriter.close();
                File outFile = new File(outFileFolder+currentTerm);
                indexWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
                NumberOfFiles++;
            }

            indexWriter.write(indexLineFormat);
//            System.out.print(indexLineFormat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void createIndexLine(ArrayList<String> indexLine, int currentTerm) {
        StringBuilder indexLineFormat = new StringBuilder();
        int NumberOfDocs = 0;
        int previousDoc = -1;
        int previousPosition = 0;
        for(String line : indexLine) {
            String[] lineSplit = line.split(" ");
            if (Integer.valueOf(lineSplit[1]) != previousDoc) {
                int gapDoc;
                if (previousDoc != -1) {
                    gapDoc = Integer.valueOf(lineSplit[1]) - previousDoc;
                } else {
                    gapDoc = Integer.valueOf(lineSplit[1]);
                }

//                indexLineFormat.append(" "+gapDoc+" "+lineSplit[2]+" "+lineSplit[3]);
                indexLineFormat.append(encoder.encode(gapDoc)+
                        encoder.encode(lineSplit[2])+
                        encoder.encode(lineSplit[3]));
                previousDoc = Integer.valueOf(lineSplit[1]);
                previousPosition = Integer.valueOf(lineSplit[3]);
                NumberOfDocs++;

            } else {
                int gapPos = Integer.valueOf(lineSplit[3]) - previousPosition;
//                indexLineFormat.append(" "+gapPos);
                indexLineFormat.append(encoder.encode(gapPos));

                previousPosition = Integer.valueOf(lineSplit[3]);
            }
        }
        indexLineFormat.append("\n");

//        writeIndexLine(NumberOfDocs+indexLineFormat.toString());
        writeIndexLine(encoder.encode(NumberOfDocs)+indexLineFormat.toString(), currentTerm);
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

        int currentTerm = 0;
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
                    else if (Integer.valueOf(line.split(" ")[0]) == currentTerm) {
                        indexLine.add(line);

                    } else {
                        next.put(runNumber, line);
                        termEnded = true;
                    }

                }
                runNumber++;
            }

            createIndexLine(indexLine, currentTerm);
            indexLine = new ArrayList<>();
            currentTerm++;
        }
        indexWriter.close();
    }
}
