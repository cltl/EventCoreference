package eu.newsreader.eventcoreference.evaluation;

import eu.newsreader.eventcoreference.util.Util;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by piek on 10/07/15.
 */
public class MakeOverviewResult {

    static String recallMacroString = "";
    static String precisionMacroString = "";
    static String fmeasureMacroString = "";
    static String recallMicroString = "";
    static String precisionMicroString = "";
    static String fmeasureMicroString = "";
    static double averageMacroRecall = 0.0;
    static double averageMacroPrecision = 0.0;
    static double averageMacroF = 0.0;
    static double averageMicroRecall = 0.0;
    static double averageMicroPrecision = 0.0;
    static double averageMicroF = 0.0;

    //results.csv
    static public void main (String[] args) {
        try {
            String pathToFolder = "";
            String method = "";
            String threshold = "";
            String corefType = "";
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--folder") && args.length>(i+1)) {
                    pathToFolder = args[i+1];
                }
                else if (arg.equals("--method") && args.length>(i+1)) {
                    method = args[i+1];
                }
                else if (arg.equals("--coref-type") && args.length>(i+1)) {
                    corefType = args[i+1].toLowerCase();
                }
                else if (arg.equals("--threshold") && args.length>(i+1)) {
                    threshold = args[i+1];
                }
            }
            recallMacroString = "Macro average recall\t\t"+threshold+"-"+method+"\n";
            precisionMacroString = "Macro average precision\t\t"+threshold+"-"+method+"\n";
            fmeasureMacroString = "Macro average f\t\t"+threshold+"-"+method+"\n";
            recallMicroString = "Micro average recall\t\t"+threshold+"-"+method+"\n";
            precisionMicroString = "Micro average precision\t\t"+threshold+"-"+method+"\n";
            fmeasureMicroString = "Micro average f\t\t"+threshold+"-"+method+"\n";

            File resultFolder = new File(pathToFolder);
            OutputStream fos = new FileOutputStream(resultFolder.getAbsolutePath()+"/"+"overall-evaluation.csv");
            ArrayList<File> corpora = Util.makeFolderList(resultFolder);
            for (int i = 0; i < corpora.size(); i++) {
                File corpus = corpora.get(i);
                File resultFile = new File (corpus.getAbsolutePath()+"/"+corefType+"/"+"results.csv");
                readResultsCsv(resultFile, method);
            }
            recallMacroString += "Average\t"+method+"\t"+averageMacroRecall/corpora.size()+"\n\n";
            precisionMacroString += "Average\t"+method+"\t"+averageMacroPrecision/corpora.size()+"\n\n";
            fmeasureMacroString += "Average\t"+method+"\t"+averageMacroF/corpora.size()+"\n\n";

            recallMicroString += "Average\t"+method+"\t"+averageMicroRecall/corpora.size()+"\n\n";
            precisionMicroString += "Average\t"+method+"\t"+averageMicroPrecision/corpora.size()+"\n\n";
            fmeasureMicroString += "Average\t"+method+"\t"+averageMicroF/corpora.size()+"\n\n";

            fos.write(recallMacroString.getBytes());
            fos.write(precisionMacroString.getBytes());
            fos.write(fmeasureMacroString.getBytes());
            fos.write(recallMicroString.getBytes());
            fos.write(precisionMicroString.getBytes());
            fos.write(fmeasureMicroString.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void readResultsCsv (File file, String method) {
        try {
            boolean MACRO = false;
            boolean MICRO = false;
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String corpusName = file.getParentFile().getParentFile().getName();
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().startsWith("Macro")) {
                    MACRO = true;
                }
                else if (inputLine.trim().startsWith("Micro")) {
                    MACRO = false;
                    MICRO = true;
                }
                else if (inputLine.trim().startsWith("Coreference")) {
                    String [] fields = inputLine.split("\t");
                    if (fields.length==4) {
                        String recall = fields[1];
                        String precision = fields[2];
                        String fmeasure = fields[3];
                        if (MACRO) {
                            recallMacroString += corpusName + "\t" + method + "\t" + recall + "\tR\n";
                            precisionMacroString += corpusName + "\t" + method + "\t" + precision + "\tP\n";
                            fmeasureMacroString += corpusName + "\t" + method + "\t" + fmeasure + "\tF\n";
                            averageMacroRecall += Double.parseDouble(recall);
                            averageMacroPrecision += Double.parseDouble(precision);
                            averageMacroF += Double.parseDouble(fmeasure);

                        }
                        if (MICRO) {
                            recallMicroString += corpusName + "\t" + method + "\t" + recall + "\tR\n";
                            precisionMicroString += corpusName + "\t" + method + "\t" + precision + "\tP\n";
                            fmeasureMicroString += corpusName + "\t" + method + "\t" + fmeasure + "\tF\n";
                            averageMicroRecall += Double.parseDouble(recall);
                            averageMicroPrecision += Double.parseDouble(precision);
                            averageMicroF += Double.parseDouble(fmeasure);
                        }
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
