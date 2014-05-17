package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 5/17/14.
 */
public class EntityTypes {

    static public final String [] PARTICIPANT = {"person", "org", "norp"};
    static public final String [] LOCATION = {"location", "gpe", "loc"};
    static public final String [] PRODUCT = {"product"};
    static public final String [] DATE = {"date"};
    static public final String [] AMOUNT = {"cardinal"};
    static public final String [] MISC = {"misc"};


    static public boolean isLOCATION (String role) {
        for (int i = 0; i < LOCATION.length; i++) {
            String s = LOCATION[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isPARTICIPANT (String role) {
        for (int i = 0; i < PARTICIPANT.length; i++) {
            String s = PARTICIPANT[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isDATE (String role) {
        for (int i = 0; i < DATE.length; i++) {
            String s = DATE[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isPRODUCT (String role) {
        for (int i = 0; i < PRODUCT.length; i++) {
            String s = PRODUCT[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isMISC (String role) {
        for (int i = 0; i < MISC.length; i++) {
            String s = MISC[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

}
