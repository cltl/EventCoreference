package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.KafParticipant;
import eu.kyotoproject.kaf.KafSense;

import java.util.ArrayList;
import java.util.Vector;

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



    static public String normalizeProbBankValue (String role) {
        String normRole = role;
        if (role.toLowerCase().startsWith("arg-")) {   ///ARG- and arg-
            normRole = "A"+role.substring(4);
        }
        else if (role.toLowerCase().startsWith("arg")) {   ///ARG and arg
            normRole = "A"+role.substring(3);
        }
        else if (role.toLowerCase().startsWith("a-")) {       /// A- and a-
            normRole = "A"+role.substring(2);
        }
        else if (role.startsWith("a")) {     //// a
            normRole = "A"+role.substring(1);
        }
        return normRole;
    }


    static public final String [] PRIMEPARTICIPANT = {"a0","arg0", "a-0", "arg-0"};
    static public final String [] SECONDPARTICIPANT = {"a1","arg1", "a-1", "arg-1"};
    static public final String [] THIRDPARTICIPANT = {"a2","arg2", "a-2", "arg-2"};
    static public final String [] NONPRIMEPARTICIPANT = {"a1", "a2", "a3", "a4", "arg1", "arg2", "arg3", "a-1", "a-2", "a-3",
            "a-4", "arg-1", "arg-2", "arg-3", "arg-4", "am-dir", "argm-dir", "argm"};
    static public final String [] LOCATION = {"am-loc", "argm-loc", "am-dir"};
    static public final String [] TIME = {"am-tmp", "argm-tmp"};


    static public boolean isROLE (String role, String[] roles) {
        for (int i = 0; i < roles.length; i++) {
            String s = roles[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean isROLE (ArrayList<String> predicates, String[] roles) {
        //System.out.println("predicates.toString() = " + predicates.toString());
        for (int i = 0; i < roles.length; i++) {
            String s = roles[i];
            for (int j = 0; j < predicates.size(); j++) {
                String s1 = predicates.get(j);
                if (s.equalsIgnoreCase(s1)) {
                    return true;
                }
                if (s1.toLowerCase().endsWith("#"+s.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }


    static public boolean isPRIMEPARTICIPANT (String role) {
        for (int i = 0; i < PRIMEPARTICIPANT.length; i++) {
            String s = PRIMEPARTICIPANT[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean hasPRIMEPARTICIPANT (ArrayList<String> roles) {
        for (int i = 0; i < PRIMEPARTICIPANT.length; i++) {
            String s = PRIMEPARTICIPANT[i];
            for (int j = 0; j < roles.size(); j++) {
                String role = roles.get(j);
                if (s.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    static public boolean isSECONDPARTICIPANT (String role) {
        for (int i = 0; i < SECONDPARTICIPANT.length; i++) {
            String s = SECONDPARTICIPANT[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean hasSECONDPARTICIPANT (ArrayList<String> roles) {
        for (int i = 0; i < SECONDPARTICIPANT.length; i++) {
            String s = SECONDPARTICIPANT[i];
            for (int j = 0; j < roles.size(); j++) {
                String role = roles.get(j);
                if (s.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }
    static public boolean isTHIRDPARTICIPANT (String role) {
        for (int i = 0; i < THIRDPARTICIPANT.length; i++) {
            String s = THIRDPARTICIPANT[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean hasTHIRDPARTICIPANT (ArrayList<String> roles) {
        for (int i = 0; i < THIRDPARTICIPANT.length; i++) {
            String s = THIRDPARTICIPANT[i];
            for (int j = 0; j < roles.size(); j++) {
                String role = roles.get(j);
                if (s.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }

   static public boolean isNONPRIMEPARTICIPANT (String role) {
        for (int i = 0; i < NONPRIMEPARTICIPANT.length; i++) {
            String s = NONPRIMEPARTICIPANT[i];
            if (s.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    static public boolean hasNONPRIMEPARTICIPANT (ArrayList<String> roles) {
        for (int i = 0; i < NONPRIMEPARTICIPANT.length; i++) {
            String s = NONPRIMEPARTICIPANT[i];
            for (int j = 0; j < roles.size(); j++) {
                String role = roles.get(j);
                if (s.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }


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
        if (isPRIMEPARTICIPANT(role) || isNONPRIMEPARTICIPANT(role)) {
            return true;
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
        if (isPARTICIPANT(role)) {
            return true;
        }
        else if (isLOCATION(role)) {
            return true;
        }
        else {
            return false;
        }
    }

    static public boolean hasFrameNetRole (KafParticipant kafParticipant) {
        for (int k = 0; k < kafParticipant.getExternalReferences().size(); k++) {
            KafSense kafSense =  kafParticipant.getExternalReferences().get(k);
            if (kafSense.getResource().equalsIgnoreCase("FrameNet")) {
                return  true;
            }
        }
        return false;
    }

    static public boolean hasSourceTarget (KafParticipant kafParticipant) {
        /**
         *           <externalRef resource="VerbNet" reference="indicate-78@Topic"/>
         <externalRef resource="FrameNet" reference="Communication@Message"/>
         <externalRef resource="FrameNet" reference="Communication@Topic"/>
         <externalRef resource="FrameNet" reference="Evidence@Proposition"/>
         */
        for (int i = 0; i < kafParticipant.getExternalReferences().size(); i++) {
            KafSense kafSense = kafParticipant.getExternalReferences().get(i);
            if (kafSense.getSensecode().endsWith("@Topic") ||
                kafSense.getSensecode().endsWith("@Message") ||
                kafSense.getSensecode().endsWith("@Theme") ||
                kafSense.getSensecode().endsWith("@Proposition")
                    ) {
                return true;
            }
        }
        return false;
    }

    static public boolean hasSourceTarget (KafParticipant kafParticipant, Vector<String> communicationVector) {
        /**
         *           <externalRef resource="VerbNet" reference="indicate-78@Topic"/>
         <externalRef resource="FrameNet" reference="Communication@Message"/>
         <externalRef resource="FrameNet" reference="Communication@Topic"/>
         <externalRef resource="FrameNet" reference="Evidence@Proposition"/>
         */
        for (int i = 0; i < kafParticipant.getExternalReferences().size(); i++) {
            KafSense kafSense = kafParticipant.getExternalReferences().get(i);
            if (communicationVector.contains(kafSense.getSensecode().toLowerCase())) {
                return true;
            }
            int idx = kafSense.getSensecode().lastIndexOf("@");
            if (idx>-1) {
                String role = kafSense.getSensecode().substring(idx);
                if (communicationVector.contains(role.toLowerCase())) {
                    //  System.out.println("role = " + role);
                    return true;
                }
            }
        }
        return false;
    }
}
