package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.KafSense;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by piek on 16/03/15.
 */
public class FrameTypes {

    static public String getEventTypeString (ArrayList<KafSense> eventConcepts,
                                      Vector<String> contextualVector,
                                      Vector<String> communicationVector,
                                      Vector<String> grammaticalVector) {
        boolean DEBUG = false;
        String eventType = "";
        //// we prefer the frames listed in the external resources
        for (int k = 0; k < eventConcepts.size(); k++) {
            KafSense kafSense = eventConcepts.get(k);
            if (kafSense.getResource().equalsIgnoreCase("framenet")) {
                if (contextualVector != null && contextualVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = "contextual";
                    break;
                }
                else if (communicationVector != null && communicationVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = "source";
                    break;
                } else if (grammaticalVector != null && grammaticalVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = "grammatical";
                    break;
                }
            }
        }
        if (DEBUG) System.out.println("eventType = " + eventType);
        //// if none of the frames matched, we check the eventtype value that was given
        if (eventType.isEmpty()) {
            for (int k = 0; k < eventConcepts.size(); k++) {
                KafSense kafSense = eventConcepts.get(k);
                if (kafSense.getResource().equalsIgnoreCase("eventtype")) {
                    if (kafSense.getSensecode().equalsIgnoreCase("speech-cognition")) {
                        eventType = "source";
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("speech_cognition")) {
                        eventType = "source";
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("speech")) {
                        eventType = "source";
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("source")) {
                        eventType = "source";
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("communication")) {
                        eventType = "source";
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("cognition")) {
                        eventType = "source";
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("grammatical")) {
                        eventType = "grammatical";
                        break;
                    } else {
                        eventType = "contextual";
                        break;
                    }
                }
            }
        }
        else {
            ///// we are going to overwrite any event type since the frame mapping is more trustworthy
            for (int k = 0; k < eventConcepts.size(); k++) {
                KafSense kafSense = eventConcepts.get(k);
                if (kafSense.getResource().equalsIgnoreCase("eventtype")) {
                    kafSense.setSensecode(eventType);
                    break;
                }
            }
        }
        if (DEBUG) System.out.println("final eventType = " + eventType);
        return eventType;
    }
}
