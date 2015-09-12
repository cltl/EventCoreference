package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 9/19/14.
 */
public class EventTypes {

    static public final String [] CONTEXTUAL = {"other", "contextual", "contextualEvent"};
    static public final String [] COMMUNICATION = {"sourceEvent","speech", "communication", "cognition", "source"};
    static public final String [] GRAMMATICAL = {"grammaticalEvent","grammatical"};
    static public final String [] FUTURE = {"futureEvent", "future"};


    static public boolean isCONTEXTUAL(String type) {
        for (int i = 0; i < CONTEXTUAL.length; i++) {
            String s = CONTEXTUAL[i];
            if (s.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isCOMMUNICATION (String type) {
        for (int i = 0; i < COMMUNICATION.length; i++) {
            String s = COMMUNICATION[i];
            if (s.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isGRAMMATICAL (String type) {
        for (int i = 0; i < GRAMMATICAL.length; i++) {
            String s = GRAMMATICAL[i];
            if (s.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isFUTURE (String type) {
        for (int i = 0; i < FUTURE.length; i++) {
            String s = FUTURE[i];
            if (s.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

}
