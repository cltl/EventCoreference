package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class InterDocumentEntityCoref {

    static public void main (String [] args) {
        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 1;
        String pathToNafFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out-tiny";
        String projectName  = "worldcup";
        String extension = ".naf";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length>(i+1)) {
                pathToNafFolder = args[i+1];
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
            else if (arg.equals("--project") && args.length>(i+1)) {
                projectName = args[i+1];
            }
        }
        processFolderForEntities(projectName, new File(pathToNafFolder), extension, conceptMatchThreshold, phraseMatchThreshold);
    }



    public static void processFolderForEntities (String project, File pathToNafFolder, String extension, double conceptMatchThreshold,
                                      double phraseMatchThreshold

    ) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemActors = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemPlaces = new ArrayList<SemObject>();

        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
        //System.out.println("files.size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (i%500==0) {
                System.out.println("i = " + i);
                System.out.println("file.getName() = " + file.getAbsolutePath());
                System.out.println("semActors = " + semActors.size());
                System.out.println("semTimes = " + semTimes.size());
                System.out.println("semPlaces = " + semPlaces.size());
            }
            mySemActors = new ArrayList<SemObject>();
            mySemTimes = new ArrayList<SemObject>();
            mySemPlaces = new ArrayList<SemObject>();
            kafSaxParser.parseFile(file.getAbsolutePath());
            GetSemFromNafFile.processNafFileForEntities(project, kafSaxParser, mySemActors, mySemPlaces, mySemTimes);

            for (int j = 0; j < mySemActors.size(); j++) {
                SemObject mySemActor = mySemActors.get(j);
                boolean merge = false;
                for (int k = 0; k < semActors.size(); k++) {
                    SemObject semActor = semActors.get(k);
                    double phraseMatch = mySemActor.matchObjectByPhrases((semActor));
                    if (phraseMatch>=phraseMatchThreshold) {
                        merge = true;
                        semActor.mergeSemObject(mySemActor);
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemActor.matchObjectByConcepts((semActor));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semActor.mergeSemObject(mySemActor);
                                break;
                            }
                        }
                    }
                }

                if (!merge) {
                    semActors.add(mySemActor);
                }
            }


            for (int j = 0; j < mySemPlaces.size(); j++) {
                SemObject mySemPlace = mySemPlaces.get(j);
                boolean merge = false;
                for (int k = 0; k < semPlaces.size(); k++) {
                    SemObject semPlace = semPlaces.get(k);
                    double phraseMatch = mySemPlace.matchObjectByPhrases((semPlace));
                    if (phraseMatch>=phraseMatchThreshold) {
                        merge = true;
                        semPlace.mergeSemObject(mySemPlace);
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemPlace.matchObjectByConcepts((semPlace));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semPlace.mergeSemObject(mySemPlace);
                                break;
                            }
                        }
                    }
                }

                if (!merge) {
                    semPlaces.add(mySemPlace);
                }
            }
            if (GetSemFromNafFile.docOwlTime.getDateString().isEmpty()) {
                semTimes.add(GetSemFromNafFile.docSemTime);
            }

            for (int j = 0; j < mySemTimes.size(); j++) {
                SemObject mySemTime = mySemTimes.get(j);
                boolean merge = false;
                for (int k = 0; k < semTimes.size(); k++) {
                    SemObject semTime = semTimes.get(k);
                    double phraseMatch = mySemTime.matchObjectByPhrases((semTime));
                    if (phraseMatch>=phraseMatchThreshold) {
                        merge = true;
                        semTime.mergeSemObject(mySemTime);
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemTime.matchObjectByConcepts((semTime));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semTime.mergeSemObject(mySemTime);
                                break;
                            }
                        }
                    }
                }

                if (!merge) {
                    semTimes.add(mySemTime);
                }
            }
        }
        File entityFolder = new File(pathToNafFolder+"/"+"entities");
        if (!entityFolder.exists()) {
            entityFolder.mkdir();
        }
        if (entityFolder.exists()) {
            try {
                //System.out.println("pathToNafFolder = " + pathToNafFolder);

                FileOutputStream fos = new FileOutputStream(entityFolder + "/sem-entities.trig");
                GetSemFromNafFile.serializeJenaEntities(fos, semActors, semPlaces, semTimes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


}
