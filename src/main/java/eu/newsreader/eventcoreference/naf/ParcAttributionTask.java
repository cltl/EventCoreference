package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.objects.PerspectiveObject;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SemRelation;
import eu.newsreader.eventcoreference.util.TimeLanguage;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by piek on 10/06/15.
 */
public class ParcAttributionTask {
    static Vector<String> communicationVector = null;
    static Vector<String> grammaticalVector = null;
    static Vector<String> contextualVector = null;
    static final public String ID_SEPARATOR = "#";
    static final public String URI_SEPARATOR = "_";


    static public void main (String [] args) {

        String pathToFolder = "";
        String extension = "";
        String comFrameFile = "";
        pathToFolder = "/Users/piek/Desktop/PerspectiveAnnotation/PARC/test/nwr_pipe_output/test";
        extension = ".naf";
        comFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v2_2015/resources/communication.txt";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--naf-folder") && ((i+1)<args.length)) {
                pathToFolder = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--extension") && ((i+1)<args.length)) {
                extension = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--communication-frames") && ((i+1)<args.length)) {
                comFrameFile = args[i+1];
            }
        }
        communicationVector = Util.ReadFileToStringVector(comFrameFile);
        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<File> files = Util.makeFlatFileList(new File(pathToFolder), extension);
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
          //  System.out.println("file.getName() = " + file.getName());
            kafSaxParser.parseFile(file);
            processNafFile(file, "", kafSaxParser);
        }
    }

    static public String processNafFile (File file, String project, KafSaxParser kafSaxParser) {
        String attribution = "";
        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<PerspectiveObject> perspectiveObjects = new ArrayList<PerspectiveObject>();
        ArrayList<SemRelation> factRelations = new ArrayList<SemRelation>();


        GetSemFromNafFile.processNafFileWithAdditionalRoles(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
        GetPerspectiveRelations.getPerspective(kafSaxParser,
                project, perspectiveObjects,
                semActors,
                contextualVector,
                communicationVector,
                grammaticalVector);
        perspectiveRelationsToEval(file, perspectiveObjects, kafSaxParser);
        return attribution;
    }

    public static void perspectiveRelationsToEval (File kafFile, ArrayList<PerspectiveObject> perspectiveObjects, KafSaxParser kafSaxParser) {
        try {
            OutputStream fos = new FileOutputStream(kafFile.getAbsolutePath()+".foreval");
            for (int i = 0; i < perspectiveObjects.size(); i++) {
                PerspectiveObject perspectiveObject = perspectiveObjects.get(i);
                if ((perspectiveObject.getTargetEventMentions().size()>0)) {
                    String id = perspectiveObject.getPredicateId()+","+
                            perspectiveObject.getSource().getId()+","+perspectiveObject.getTarget().getId()+"#"+perspectiveObject.getNafMention().getSentence();

                    String str = "";


                    str += id+"\t"+"Content";
                    ArrayList<String> tokens = kafSaxParser.convertTermSpanToTokenSpan(perspectiveObject.getTarget().getSpanIds());
                    for (int j = 0; j < tokens.size(); j++) {
                        String span = tokens.get(j);
                        str += " "+span;
                    }
                    str += "\n";

                    str += id+"\t"+"Cue";
                    tokens = kafSaxParser.convertTermSpanToTokenSpan(perspectiveObject.getPredicateSpanIds());
                    for (int j = 0; j < tokens.size(); j++) {
                        String span = tokens.get(j);
                        str += " "+span;
                    }
                    str += "\n";

                    str += id+"\t"+"Source";
                    tokens = kafSaxParser.convertTermSpanToTokenSpan(perspectiveObject.getSource().getSpanIds());
                    for (int j = 0; j < tokens.size(); j++) {
                        String span = tokens.get(j);
                        str += " "+span;
                    }
                    str += "\n";

                    str += "\n";

                    //System.out.println(str);
                    fos.write(str.getBytes());
                }
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
