package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.MD5Checksum;
import org.apache.jena.atlas.logging.Log;

import java.util.ArrayList;

/**
 * Created by piek on 2/12/14.
 */
public class GetSemFromNafStream {

    static public void main(String[] args) {
        Log.setLog4j("jena-log4j.properties");
        NafSemParameters nafSemParameters = new NafSemParameters(args);


        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(System.in);
        if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            //System.err.println("ERROR! Empty url in header NAF. Cannot create unique URIs! Aborting");
            try {
                String checkSum = MD5Checksum.getMD5ChecksumFromString(kafSaxParser.rawText);
              //  System.err.println("checkSum = " + checkSum);
                kafSaxParser.getKafMetaData().setUrl(checkSum);
            } catch (Exception e) {
             //   e.printStackTrace();
            }
        }
        GetSemFromNaf.processNafFile(nafSemParameters, kafSaxParser, semEvents, semActors, semTimes, semRelations );
        ArrayList<CompositeEvent> compositeEventArraylist = new ArrayList<CompositeEvent>();
        for (int j = 0; j < semEvents.size(); j++) {
            SemEvent mySemEvent = (SemEvent) semEvents.get(j);
            ArrayList<SemTime> myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
            ArrayList<SemActor> myActors = ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
            ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
            CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
            if (myTimes.size() <= nafSemParameters.getTIMEEXPRESSIONMAX()) {
                if (compositeEvent.isValid()) {
                    FrameTypes.setEventTypeString(compositeEvent.getEvent(), nafSemParameters);
                    compositeEventArraylist.add(compositeEvent);
                }
                else {
/*
                    System.out.println("Skipping EVENT due to no time anchor and/or no participant");
                    System.out.println("compositeEvent = " + compositeEvent.getEvent().getURI());
                    System.out.println("myTimes = " + myTimes.size());
                    System.out.println("myActors = " + myActors.size());
                    System.out.println("myRelations = " + myRelations.size());
*/
                }
            } else {
/*
                System.out.println("Skipping event due to excessive time expressions linked to it");
                System.out.println("compositeEvent = " + compositeEvent.getEvent().getURI());
                System.out.println("myTimes.size() = " + myTimes.size());
*/
            }
        }
        if (!nafSemParameters.isPERSPECTIVE()) {
            JenaSerialization.serializeJenaCompositeEvents(System.out, compositeEventArraylist, null, nafSemParameters.isILIURI(), nafSemParameters.isVERBOSE());
        }
        else {
            ArrayList<PerspectiveObject> sourcePerspectives = GetPerspectiveRelations.getSourcePerspectives(kafSaxParser,
                    semActors,
                    semEvents,
                    nafSemParameters);
            ArrayList<PerspectiveObject> documentPerspectives = GetPerspectiveRelations.getAuthorPerspectives(
                    kafSaxParser, nafSemParameters.getPROJECT(), sourcePerspectives);
            JenaSerialization.serializeJenaCompositeEventsAndPerspective(System.out, compositeEventArraylist, kafSaxParser, nafSemParameters.getPROJECT(), sourcePerspectives, documentPerspectives);
        }
    }
}