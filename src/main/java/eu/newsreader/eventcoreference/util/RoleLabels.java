package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 5/11/14.
 */
public class RoleLabels {


    static public final String [] PARTICIPANT = {"a0", "a1", "a2", "a3", "arg0", "arg1", "arg2", "arg3"};
    static public final String [] LOCATION = {"AM-LOC"};
    static public final String [] TIME = {"AM-TMP"};


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

    static public boolean isTIME (String role) {
        for (int i = 0; i < TIME.length; i++) {
            String s = TIME[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean validRole (String role) {
        if (role.equalsIgnoreCase("a0")) {
            return true;
        }
        else if (role.equalsIgnoreCase("a1")) {
            return true;
        }
        else if (role.equalsIgnoreCase("a2")) {
            return true;
        }
        else if (role.equalsIgnoreCase("a3")) {
            return true;
        }
        else if (role.equalsIgnoreCase("arg0")) {
            return true;
        }
        else if (role.equalsIgnoreCase("arg1")) {
            return true;
        }
        else if (role.equalsIgnoreCase("arg2")) {
            return true;
        }
        else if (role.equalsIgnoreCase("arg3")) {
            return true;
        }
        else if (role.equalsIgnoreCase("am-loc")) {
            return true;
        }
        else if (role.equalsIgnoreCase("am-tmp")) {
            return true;
        }
        else {
            return false;
        }
    }


}
