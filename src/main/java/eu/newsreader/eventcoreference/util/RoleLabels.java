package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 5/11/14.
 */
public class RoleLabels {

    /**
     * semRole="A0"     agent
     semRole="A1"       patient
     semRole="A2"       instrument, benefactive, attribute
     semRole="A3"       starting point, benefactive, attribute
     semRole="A4"       ending point
     semRole="A5"
     semRole="AA"      secondary argument, "John walked his dog"
     AM                modifier
     semRole="AM-ADV"  adverbials
     semRole="AM-CAU"  causative
     semRole="AM-DIR"  directional
     semRole="AM-DIS"  discourse
     semRole="AM-EXT"  extent, amount of change "raised prices by 15%"
     semRole="AM-LOC"  locative
     semRole="AM-MNR"  manner
     semRole="AM-MOD"  modal
     semRole="AM-NEG"  negation
     semRole="AM-PNC"
     semRole="AM-PRD"  purpose
     semRole="AM-REC"  reciprocals
     semRole="AM-TMP"  temporal
     semRole="C-A0"
     semRole="C-A1"
     semRole="C-A2"
     semRole="R-A0"
     semRole="R-A1"
     semRole="R-A2"
     semRole="R-A3"
     semRole="R-A4"
     semRole="R-AA"
     semRole="R-AM-CAU"
     semRole="R-AM-EXT"
     semRole="R-AM-LOC"
     semRole="R-AM-MNR"
     semRole="R-AM-PNC"
     semRole="R-AM-TMP"

     COM comitatives "I sang a song with my sister [COM]"
     GOL goal "I fed the cat for her mother [GOL]"
     PRD secondary predicate "She kicked the locker shut [PRD]"
     DSP direct speech
     LVB light verb

     */

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
