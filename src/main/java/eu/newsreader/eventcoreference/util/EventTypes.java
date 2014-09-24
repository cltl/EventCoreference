package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 9/19/14.
 */
public class EventTypes {

    static public final String [] CONTEXTUAL = {"other", "contextual"};
    static public final String [] COMMUNICATION = {"speech", "communication", "cognition"};
    static public final String [] GRAMMATICAL = {"grammatical"};


    static public boolean isCONTEXTUAL(String role) {
        for (int i = 0; i < CONTEXTUAL.length; i++) {
            String s = CONTEXTUAL[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isCOMMUNICATION (String role) {
        for (int i = 0; i < COMMUNICATION.length; i++) {
            String s = COMMUNICATION[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isGRAMMATICAL (String role) {
        for (int i = 0; i < GRAMMATICAL.length; i++) {
            String s = GRAMMATICAL[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

}
