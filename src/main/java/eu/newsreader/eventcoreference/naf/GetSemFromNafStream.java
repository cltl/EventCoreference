package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SemRelation;
import eu.newsreader.eventcoreference.output.JenaSerialization;

import java.util.ArrayList;

/**
 * Created by piek on 2/12/14.
 */
public class GetSemFromNafStream {

    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        String projectName  = "test";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--project") && args.length>(i+1)) {
                projectName = args[i+1];
            }
        }
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(System.in);
        boolean ADDITIONAlROLES = true;
        GetSemFromNafFile.processNafFile(projectName, kafSaxParser, semEvents, semActors, semTimes, semRelations, ADDITIONAlROLES);
        JenaSerialization.serializeJena(System.out,
                semEvents,
                semActors,
                semTimes,
                semRelations,
                null,
                false);
    }
}
