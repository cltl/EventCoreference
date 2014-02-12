package eu.newsreader.eventcoreference.util;

import eu.newsreader.eventcoreference.naf.InterDocumentEventCoref;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by kyoto on 12/13/13.
 */
public class ProcessNewsReaderTechCrunchBatch {
    static String extension = ".naf";
    static double conceptMatchThreshold = 0.6;
    static double phraseMatchThreshold = 0.6;

    static public void main (String [] args) {
        String pathToNafClusters = "/Users/piek/Desktop/NWR-DATA/techcrunch/clusters1_3000";
        String projectName  = "techcrunch";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--cluster-folders") && args.length>(i+1)) {
                pathToNafClusters = args[i+1];
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

        File nafClusterFolder = new File (pathToNafClusters);
        if (nafClusterFolder.exists()) {
            ArrayList<File> folders = Util.makeFolderList(nafClusterFolder);
            for (int i = 0; i < folders.size(); i++) {
                File pathToNafFolder =  folders.get(i);
               // System.out.println("pathToNafFolder.getName() = " + pathToNafFolder.getName());
                InterDocumentEventCoref.processFolder (projectName, pathToNafFolder, extension, conceptMatchThreshold, phraseMatchThreshold);
            }
        }
        else {
            System.out.println("Cannot find nafClusterFolder.getAbsolutePath() = " + nafClusterFolder.getAbsolutePath());
        }
    }

}
