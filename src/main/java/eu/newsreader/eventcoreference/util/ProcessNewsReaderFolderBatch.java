package eu.newsreader.eventcoreference.util;

import eu.newsreader.eventcoreference.naf.InterDocumentEventCoref;

import java.io.File;

/**
 * Created by kyoto on 12/13/13.
 */
public class ProcessNewsReaderFolderBatch {
    static String extension = ".naf";
    static double conceptMatchThreshold = 0.6;
    static double phraseMatchThreshold = 0.6;

    static public void main (String [] args) {
        String pathToNafFolder = "";
        String projectName  = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--folder") && args.length>(i+1)) {
                pathToNafFolder = args[i+1];
            }
            else if (arg.equals("--project") && args.length>(i+1)) {
                projectName = args[i+1];
            }
            else if (arg.equals("--extension") && args.length>(i+1)) {
                extension = args[i+1];
            }
            else if (arg.equals("--concept-match") && args.length>(i+1)) {
                conceptMatchThreshold = Double.parseDouble(args[i+1]);
            }
            else if (arg.equals("--phrase-match") && args.length>(i+1)) {
                phraseMatchThreshold = Double.parseDouble(args[i+1]);
            }
        }

        File nafFolder = new File (pathToNafFolder);
        if (nafFolder.exists()) {
                InterDocumentEventCoref.processFolder (projectName, nafFolder, extension, conceptMatchThreshold, phraseMatchThreshold, null);
        }
        else {
            System.out.println("Cannot find nafFolder.getAbsolutePath() = " + nafFolder.getAbsolutePath());
        }
    }

}
