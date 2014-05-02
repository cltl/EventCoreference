package eu.newsreader.eventcoreference.util;

import java.io.File;

/**
 * Created by kyoto on 12/13/13.
 */
public class ProcessNewsReaderFolderBatch {
    static String extension = ".naf";
    static double conceptMatchThreshold = 0;
    static double phraseMatchThreshold = 1;

    static public void main (String [] args) {
        String pathToNafFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out";
        String projectName  = "worldcup";
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
          //  LargeInterDocumentEventCoref.processFolder(projectName, nafFolder, extension, conceptMatchThreshold, phraseMatchThreshold, null);

        }
        else {
            System.out.println("Cannot find nafFolder.getAbsolutePath() = " + nafFolder.getAbsolutePath());
        }
    }

}
