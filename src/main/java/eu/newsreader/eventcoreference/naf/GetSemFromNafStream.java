package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;

import java.util.ArrayList;

/**
 * Created by piek on 2/12/14.
 */
public class GetSemFromNafStream {

    static public void main(String[] args) {
        boolean ADDITIONAlROLES = false;

        String projectName = "no-project";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--project") && args.length > (i + 1)) {
                projectName = args[i + 1];
            }
            if (arg.equals("--additional-roles") && args.length > (i + 1)) {
                ADDITIONAlROLES = true;
            }
        }
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(System.in);


        if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            System.out.println("WARNING! Empty url in header NAF with the file name! Aborting");
        }
        else {
            GetSemFromNafFile.processNafFile(projectName, kafSaxParser, semEvents, semActors, semTimes, semRelations, ADDITIONAlROLES);
            ArrayList<CompositeEvent> compositeEventArraylist = new ArrayList<CompositeEvent>();
            for (int j = 0; j < semEvents.size(); j++) {
                SemEvent mySemEvent = (SemEvent) semEvents.get(j);
                ArrayList<SemTime> myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
                ArrayList<SemActor> myActors = ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
                ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
                CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
                if (myTimes.size()<=ClusterEventObjects.TIMEEXPRESSIONMAX) {
                    if (compositeEvent.isValid()) {
                        compositeEventArraylist.add(compositeEvent);
                    }
                }
            }
            JenaSerialization.serializeJenaCompositeEvents(System.out, compositeEventArraylist, null, false);
        }
    }
}