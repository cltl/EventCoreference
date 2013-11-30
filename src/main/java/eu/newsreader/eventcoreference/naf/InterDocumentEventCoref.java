package eu.newsreader.eventcoreference.naf;

import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SemRelation;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class InterDocumentEventCoref {
    static final String layer = "sem-instances";
    static final String name = "vua-event-coref-interdoc-time-lemma-baseline";
    static final String version = "1.0";
    static ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
    static ArrayList<SemObject> semActors = new ArrayList<SemObject>();
    static ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
    static ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
    static ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();

    static public void main (String [] args) {
        semEvents = new ArrayList<SemObject>();
        semActors = new ArrayList<SemObject>();
        semTimes = new ArrayList<SemObject>();
        semPlaces = new ArrayList<SemObject>();
        semRelations = new ArrayList<SemRelation>();

        String pathToNafFolder = "";
            String extension = "";
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--naf-folder") && args.length>(i+1)) {
                    pathToNafFolder = args[i+1];
                }
                else if (arg.equals("--extension") && args.length>(i+1)) {
                    extension = args[i+1];
                }
            }
            ArrayList<File> files = Util.makeRecursiveFileList(new File(pathToNafFolder), extension);
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);

                ArrayList<SemObject> mySemEvents = new ArrayList<SemObject>();
                ArrayList<SemObject> mySemActors = new ArrayList<SemObject>();
                ArrayList<SemObject> mySemTimes = new ArrayList<SemObject>();
                ArrayList<SemObject> mySemPlaces = new ArrayList<SemObject>();
                ArrayList<SemRelation> mySemRelations = new ArrayList<SemRelation>();
                GetSemFromNafFile.processNafFile(file.getAbsolutePath(), mySemEvents, mySemActors, mySemTimes, mySemPlaces, mySemRelations);

            }
    }




}
