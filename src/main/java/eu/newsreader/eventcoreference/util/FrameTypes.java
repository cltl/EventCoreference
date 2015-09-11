package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.objects.SemObject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by piek on 16/03/15.
 */
public class FrameTypes {

    static public final String SOURCE = "sourceEvent";
    static public final String GRAMMATICAL = "grammaticalEvent";
    static public final String CONTEXTUAL = "contextualEvent";

    static public String getEventTypeString(ArrayList<KafSense> eventConcepts,
                                            Vector<String> contextualVector,
                                            Vector<String> sourceVector,
                                            Vector<String> grammaticalVector) {
        boolean DEBUG = false;
        String eventType = "";
        //// we prefer the frames listed in the external resources
        for (int k = 0; k < eventConcepts.size(); k++) {
            KafSense kafSense = eventConcepts.get(k);
            if (kafSense.getResource().equalsIgnoreCase("framenet")) {
                if (contextualVector != null && contextualVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = CONTEXTUAL;
                    break;
                } else if (sourceVector != null && sourceVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = SOURCE;
                    break;
                } else if (grammaticalVector != null && grammaticalVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = GRAMMATICAL;
                    break;
                }
            }
        }
        if (DEBUG) System.out.println("eventType = " + eventType);
        //// if none of the frames matched or there is no frame info, we check the eventtype value that was given
        if (eventType.isEmpty()) {
            for (int k = 0; k < eventConcepts.size(); k++) {
                KafSense kafSense = eventConcepts.get(k);
                if (kafSense.getResource().equalsIgnoreCase("eventtype")) {
                    if (kafSense.getSensecode().equalsIgnoreCase("speech-cognition")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("speech_cognition")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("speech")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("source")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("communication")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("cognition")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("grammatical")) {
                        eventType = GRAMMATICAL;
                        break;
                    } else {
                        eventType = CONTEXTUAL;
                        break;
                    }
                }
            }
        }
        if (DEBUG) System.out.println("final eventType = " + eventType);
        return eventType;
    }

    static public String setEventTypeString(SemObject semEvent,
                                          Vector<String> contextualVector,
                                          Vector<String> sourceVector,
                                          Vector<String> grammaticalVector) {
        boolean DEBUG = false;
        String eventType = CONTEXTUAL;
        //// we prefer the frames listed in the external resources
        for (int k = 0; k < semEvent.getConcepts().size(); k++) {
            KafSense kafSense = semEvent.getConcepts().get(k);
            if (kafSense.getResource().equalsIgnoreCase("framenet")) {
                if (contextualVector != null && contextualVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = CONTEXTUAL;
                    break;
                } else if (sourceVector != null && sourceVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = SOURCE;
                    break;
                } else if (grammaticalVector != null && grammaticalVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventType = GRAMMATICAL;
                    break;
                }
            }
        }
        if (DEBUG) System.out.println("eventType = " + eventType);
        //// if none of the frames matched or there is no frame info, we check the eventtype value that was given
        if (eventType.isEmpty()) {
            for (int k = 0; k < semEvent.getConcepts().size(); k++) {
                KafSense kafSense = semEvent.getConcepts().get(k);
                if (kafSense.getResource().equalsIgnoreCase("eventtype")) {
                    if (kafSense.getSensecode().equalsIgnoreCase("speech-cognition")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("speech_cognition")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("speech")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("source")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("communication")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("cognition")) {
                        eventType = SOURCE;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("grammatical")) {
                        eventType = GRAMMATICAL;
                        break;
                    } else {
                        eventType = CONTEXTUAL;
                        break;
                    }
                }
            }
        }
        if (DEBUG) System.out.println("final eventType = " + eventType);
        ArrayList<KafSense> typedConcepts = new ArrayList<KafSense>();
        KafSense typeSense = new KafSense();
        typeSense.setResource("nwr");
        typeSense.setSensecode(eventType);
        typedConcepts.add(typeSense);
        for (int i = 0; i < semEvent.getConcepts().size(); i++) {
            KafSense kafSense = semEvent.getConcepts().get(i);
            if (!kafSense.getResource().equalsIgnoreCase("eventtype")) {
                typedConcepts.add(kafSense);
            }
        }
        semEvent.setConcept(typedConcepts);
        return eventType;
    }

}
