package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 5/17/14.
 */
public class EntityTypes {

    /**
     * type="CARDINAL">
     type="DATE">
     type="EVENT">
     type="FAC">
     type="GPE">
     type="LANGUAGE">
     type="LAW">
     type="LOC">
     type="MONEY">
     type="NORP">
     type="ORDINAL">
     type="ORG">
     type="PERCENT">
     type="PERSON">
     type="PRODUCT">
     type="QUANTITY">
     type="TIME">
     type="WORK_OF_ART">
     */
    static public final String [] PARTICIPANT = {"person", "org", "norp"};
    static public final String [] LOCATION = {"location", "gpe", "loc"};
    static public final String [] PRODUCT = {"product", "work_of_art"};
    static public final String [] DATE = {"date", "time"};
    static public final String [] AMOUNT = {"cardinal", "quantity", "percent", "ordinal", "money"};
    static public final String [] MISC = {"misc", "law", "fac", "event", "language"};


    static public boolean isLOCATION (String role) {
        for (int i = 0; i < LOCATION.length; i++) {
            String s = LOCATION[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }


    static public boolean isAMOUNT (String role) {
        for (int i = 0; i < AMOUNT.length; i++) {
            String s = AMOUNT[i];
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
