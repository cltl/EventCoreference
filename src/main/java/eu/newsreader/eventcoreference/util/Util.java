package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {
    static final int SPANMATCHTHRESHOLD = 75;
    static public final int SPANMAXTIME = 10;
    static public final int SPANMAXLOCATION= 10;
    static public final int SPANMINLOCATION = 2;
    static public final int SPANMAXPARTICIPANT = 6;
    static public final int SPANMINPARTICIPANT = 2;
    static public final int SPANMAXCOREFERENTSET = 5;

    /**
     * Required to be able to write Composite SemEvent Objects to existing object files
     */
    static public class AppendableObjectOutputStream extends ObjectOutputStream {

        public AppendableObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            // do not write a header
            reset();
        }

    }

    /**
     * all mentions are checked against the stored relations if the value and the event are already stored.
     * If so only the mention is added, otherwise a new fact relation is created
     * @param nafMention
     * @param factRelations
     * @param factValue
     * @param baseUrl
     * @param subjectId
     */
    static public void addMentionToFactRelations (NafMention nafMention,
                                                  ArrayList<SemRelation> factRelations,
                                                  String factValue,
                                                  String baseUrl,
                                                  String subjectId) {
        boolean valueMatch = false;
        for (int i = 0; i < factRelations.size(); i++) {
            SemRelation semRelation = factRelations.get(i);
            if ((semRelation.getObject().equalsIgnoreCase(factValue)) &&
                 (semRelation.getSubject().equals(subjectId))
               ){
                valueMatch = true;
                semRelation.addMention(nafMention);
                break;
            }
        }
        if (!valueMatch) {
            SemRelation semRelation = new SemRelation();
            //String relationInstanceId = baseUrl+"factValue_"+factRelations.size()+1;
            String relationInstanceId = baseUrl+"fv"+factRelations.size()+1;  // shorter form for triple store
            semRelation.setId(relationInstanceId);
            semRelation.addMention(nafMention);
            semRelation.addPredicate("hasFactBankValue");
            semRelation.setSubject(subjectId);
            semRelation.setObject(nafMention.getFactuality().getPrediction());
            factRelations.add(semRelation);
        }
    }

    /**
     * A single termId overlap is sufficient to fire true
     * @param objects
     * @param object
     * @return
     */
    static public boolean hasMentionAndSpanIntersect (ArrayList<SemObject> objects, SemObject object) {
        for (int i = 0; i < object.getNafMentions().size(); i++) {
            NafMention nafMention = object.getNafMentions().get(i);
            for (int j = 0; j < objects.size(); j++) {
                SemObject semObject = objects.get(j);
                for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                    String temdId = nafMention.getTermsIds().get(k);
                    for (int l = 0; l < semObject.getNafMentions().size(); l++) {
                        NafMention mention = semObject.getNafMentions().get(l);
                        if (mention.getTermsIds().contains(temdId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    static public void addObject(ArrayList<SemObject> objects, SemObject object) {
       if (!hasObject(objects, object)) {
           objects.add(object);
       }
       else {
           absorbObject(objects, object);
       }
    }

    static public void absorbObject(ArrayList<SemObject> objects, SemObject object) {
        for (int i = 0; i < objects.size(); i++) {
            SemObject semObject = objects.get(i);
            if (semObject.getURI().equals(object.getURI())) {
                for (int j = 0; j < object.getNafMentions().size(); j++) {
                    NafMention nafMention = object.getNafMentions().get(j);
                    /// add all mentions of this object that are new
                    if (!semObject.hasMention(nafMention)) {
                        semObject.addMentionUri(nafMention);
                    }
                }
                break;
            }
            else {
                //// Next check absorbs an object if there is any mention overlap despite the URI mismatch!!!!
                boolean mentionMatch = false;

                for (int j = 0; j < object.getNafMentions().size(); j++) {
                    NafMention nafMention = object.getNafMentions().get(j);
                    if (semObject.hasMention(nafMention)) {
                        mentionMatch = true;
                        break;
                    }
                }

                if (mentionMatch) {
                    /// add all mentins to this object that are new
                    for (int j = 0; j < object.getNafMentions().size(); j++) {
                        NafMention nafMention = object.getNafMentions().get(j);
                        if (!semObject.hasMention(nafMention)) {
                        }
                    }
                    break;
                }
            }
        }
    }

    static public boolean hasObject(ArrayList<SemObject> objects, SemObject object) {
        for (int i = 0; i < objects.size(); i++) {
            SemObject semObject = objects.get(i);
            if (semObject.getURI().equals(object.getURI())) {
                 return true;
            }
            else {
                //// Next check absorbs an object if there is a mention overlap despite the URI mismatch!!!!
                for (int j = 0; j < object.getNafMentions().size(); j++) {
                    NafMention nafMention = object.getNafMentions().get(j);
                    if (semObject.hasMention(nafMention)) {
                        //// there is a mention overlap!
                        return true;
                    }
                }
            }
        }
        return false;
    }
    static public boolean hasObjectUri(ArrayList<SemObject> objects, String objectURI) {
        for (int i = 0; i < objects.size(); i++) {
            SemObject semObject = objects.get(i);
            if (semObject.getURI().equals(objectURI)) {
                 return true;
            }
        }
        return false;
    }

    static public boolean hasObjectUriDEBUG(ArrayList<SemObject> objects, String objectURI) {
        System.out.println("objectURI = " + objectURI);
        for (int i = 0; i < objects.size(); i++) {
            SemObject semObject = objects.get(i);
            System.out.println("semObject.getURI() = " + semObject.getURI());
            if (semObject.getURI().equals(objectURI)) {
                System.out.println("MATCH");
                 return true;
            }
        }
        System.out.println("NO MATCH");
        return false;
    }


    static public ArrayList<KafSense> getExternalReferences(ArrayList<KafEntity> entities) {
        ArrayList<KafSense> refs = new ArrayList<KafSense>();
        for (int i = 0; i < entities.size(); i++) {
            KafEntity kafEntity = entities.get(i);
            if (!kafEntity.getType().isEmpty()) {
                KafSense entityType = new KafSense();
                entityType.setSensecode(kafEntity.getType());
                refs.add(entityType);
            }
            for (int j = 0; j < kafEntity.getExternalReferences().size(); j++) {
                KafSense kafSense = kafEntity.getExternalReferences().get(j);
                boolean match = false;
                for (int k = 0; k < refs.size(); k++) {
                    KafSense sense = refs.get(k);
                    if (sense.getSensecode().equals(kafSense.getSensecode())) {
                       match = true;
                        break;
                    }
                }
                if (!match) {
                    refs.add(kafSense);
                }
            }
        }
        return refs;
    }

    static public ArrayList<KafSense> getExternalReferencesSrlEvents (KafSaxParser kafSaxParser, KafCoreferenceSet kafCoreferenceSet) {
        ArrayList<KafSense> references = new ArrayList<KafSense>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            boolean match = false;
            for (int j = 0; j < kafEvent.getSpanIds().size(); j++) {
                String termId = kafEvent.getSpanIds().get(j);
                for (int k = 0; k < kafCoreferenceSet.getSetsOfSpans().size(); k++) {
                    ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(k);
                    for (int l = 0; l < corefTargets.size(); l++) {
                        CorefTarget corefTarget = corefTargets.get(l);
                        if (corefTarget.getId().equals(termId)) {
                            match = true;
                            break;
                        }

                    }
                }
            }
            if (match) {
                references.addAll(kafEvent.getExternalReferences());
            }
        }

        return references;
    }


    static public ArrayList<KafSense> getExternalReferencesSrlParticipants (KafSaxParser kafSaxParser, String roleId) {
        ArrayList<KafSense> references = new ArrayList<KafSense>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (kafParticipant.getId().equals(roleId)) {
                    references.addAll(kafParticipant.getExternalReferences());
                }
            }
        }
        return references;
    }



    /**
     * HACK FUNCTION BECASUE THERE IS YET NO COREFERENCE SET FOR TIME, WHEN THIS IS IN NAF WE CAN DEPRECATE THIS FUNCTION
     * @param kafSaxParser
     * @return
     */
    static public HashMap<String, ArrayList<ArrayList<CorefTarget>>> getTimeMentionsHashMapFromSrl (KafSaxParser kafSaxParser, OwlTime docOwlTime) {
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentions = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (kafParticipant.getSpans().size()>SPANMAXTIME) {
                    continue;
                }
                if (RoleLabels.isTIME(kafParticipant.getRole())) {
                    kafParticipant.setTokenStrings(kafSaxParser);
                    if (mentions.containsKey(kafParticipant.getTokenString())) {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = mentions.get(kafParticipant.getTokenString());
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(kafParticipant.getTokenString(), srlTargets);
                    } else {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(kafParticipant.getTokenString(), srlTargets);
                    }
                }
            }
        }
        return mentions;
    }

    /**

     */
    static public HashMap<String, ArrayList<ArrayList<CorefTarget>>> getLocationMentionsHashMapFromSrl (KafSaxParser kafSaxParser) {
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentions = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (!RoleLabels.hasFrameNetRole(kafParticipant)) {
                    ///// SKIP ROLE WITHOUT FRAMENET
                    continue;
                }
                //// SKIP LARGE PHRASES
                if (kafParticipant.getSpans().size()>SPANMAXLOCATION) {
                    continue;
                }
                if (kafParticipant.getSpans().size()<SPANMINLOCATION) {
                    continue;
                }
                if (RoleLabels.isLOCATION(kafParticipant.getRole())) {
                    kafParticipant.setTokenStrings(kafSaxParser);
                    if (Util.hasAlphaNumeric(kafParticipant.getTokenString())) {
                        String uri = "";
                        try {
                            uri = URLEncoder.encode(kafParticipant.getTokenString(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            // e.printStackTrace();
                        }
                        if (!uri.isEmpty()) {
                            if (mentions.containsKey(uri)) {
                                ArrayList<ArrayList<CorefTarget>> srlTargets = mentions.get(uri);
                                srlTargets.add(kafParticipant.getSpans());
                                mentions.put(uri, srlTargets);
                            } else {
                                ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                                srlTargets.add(kafParticipant.getSpans());
                                mentions.put(uri, srlTargets);
                            }
                        }
                    }
                }
            }
        }
        return mentions;
    }

    /**
     * Creates a HashMap for actor uri's with an array list of coreftargets
     * @param kafSaxParser
     * @return
     */
    static public HashMap<String, ArrayList<ArrayList<CorefTarget>>> getActorCoreftargetSetsHashMapFromSrl(KafSaxParser kafSaxParser) {
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentions = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (!RoleLabels.hasFrameNetRole(kafParticipant)) {
                    ///// SKIP ROLE WITHOUT FRAMENET
                    continue;
                }
                //// SKIP LARGE PHRASES
                if (kafParticipant.getSpans().size()>SPANMAXPARTICIPANT) {
                    continue;
                }
                if (kafParticipant.getSpans().size()<SPANMINPARTICIPANT) {
                    continue;
                }
                if (RoleLabels.isPARTICIPANT(kafParticipant.getRole())) {
                    kafParticipant.setTokenStrings(kafSaxParser);
                    if (Util.hasAlphaNumeric(kafParticipant.getTokenString())) {
                        String uri = "";
                        try {
                            uri = URLEncoder.encode(kafParticipant.getTokenString(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                           //  e.printStackTrace();
                        }
                        if (!uri.isEmpty()) {
                            if (mentions.containsKey(uri)) {
                                //   System.out.println("srl = " + srl);
                                ArrayList<ArrayList<CorefTarget>> srlTargets = mentions.get(uri);
                                srlTargets.add(kafParticipant.getSpans());
                                mentions.put(uri, srlTargets);
                            } else {
                                ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                                srlTargets.add(kafParticipant.getSpans());
                                mentions.put(uri, srlTargets);
                            }
                        }
                        else {
                        //    System.out.println("kafParticipant.getRole() = " + kafParticipant.getRole());
                        //    System.out.println("kafParticipant.getTokenString() = " + kafParticipant.getTokenString());
                        }
                    }
                    else {
                      //  System.out.println("kafParticipant.getRole() = " + kafParticipant.getRole());
                     //   System.out.println("kafParticipant.getTokenString() = " + kafParticipant.getTokenString());
                    }
                }

            }
        }
        return mentions;
    }


    /**
     * We assume entities can have a range of spans but such a range can be part of a coreference set. If so we extend the spans with the full coreference set
     * @param entitySpans
     * @param coreferenceSets
     * @return
     */
    static public ArrayList<ArrayList<CorefTarget>> getCorefTargetSetsForEntitySpans(ArrayList<ArrayList<CorefTarget>> entitySpans,
                                                                                     ArrayList<KafCoreferenceSet> coreferenceSets) {
        ArrayList<ArrayList<CorefTarget>> corefSet = new ArrayList<ArrayList<CorefTarget>>();
        for (int i = 0; i < entitySpans.size(); i++) {
            ArrayList<CorefTarget> corefTargets = entitySpans.get(i);
            corefSet.add(corefTargets);
            for (int j = 0; j < coreferenceSets.size(); j++){
                KafCoreferenceSet kafCoreferenceSet = coreferenceSets.get(j);
                if (intersectingWithAtLeastOneSetOfSpans(corefTargets, kafCoreferenceSet.getSetsOfSpans())) {
                    for (int k = 0; k < kafCoreferenceSet.getSetsOfSpans().size(); k++) {
                        ArrayList<CorefTarget> targets = kafCoreferenceSet.getSetsOfSpans().get(k);
                        if (targets.size()<=SPANMAXCOREFERENTSET) {
                            //// WE SKIP ABSURD SPANS FOR COREFERENCE
                            if (!hasCorefTargetArrayList(targets, corefSet)) {
                                corefSet.add(targets);
                            }
                        }
                    }
                }
                else {
                    ////
                }
            }
        }
       // System.out.println("corefSet.size() = " + corefSet.size());
        return corefSet;
    }

    /**
     *
     * @param eventSpan
     * @param coreferenceSets
     * @return
     */
    static public ArrayList<ArrayList<CorefTarget>> getCorefTargetSetsForEventSpans(ArrayList<CorefTarget> eventSpan,
                                                                                    ArrayList<KafCoreferenceSet> coreferenceSets) {
        ArrayList<ArrayList<CorefTarget>> corefSet = new ArrayList<ArrayList<CorefTarget>>();
        corefSet.add(eventSpan);
        for (int j = 0; j < coreferenceSets.size(); j++){
            KafCoreferenceSet kafCoreferenceSet = coreferenceSets.get(j);
            if (matchingAtLeastOneSetOfSpans(eventSpan, kafCoreferenceSet.getSetsOfSpans())) {
                for (int k = 0; k < kafCoreferenceSet.getSetsOfSpans().size(); k++) {
                    ArrayList<CorefTarget> targets = kafCoreferenceSet.getSetsOfSpans().get(k);
                    if (!hasCorefTargetArrayList(targets, corefSet)) {
                        corefSet.add(targets);
                    }
                }
            }
        }
        return corefSet;
    }

    /**
     * Checks if a lists of CorefTargets is already include in a list of lists of CorefTargets
     * @param newSpans
     * @param oldSpanSets
     * @return
     */
    static public boolean hasCorefTargetArrayList (ArrayList<CorefTarget> newSpans, ArrayList<ArrayList<CorefTarget>> oldSpanSets) {
        for (int j = 0; j < oldSpanSets.size(); j++) {
            ArrayList<CorefTarget> corefTargets = oldSpanSets.get(j);
            if (matchAllSpans(newSpans, corefTargets)) {
                /// already there, so we are done
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a list of CorefTargets has at least one intersecting span with one of the other lists of CorefTargets
     * @param spans1
     * @param spans2
     * @return
     */
    static public boolean intersectingAtLeastOneSetOfSpans (ArrayList<ArrayList<CorefTarget>> spans1, ArrayList<ArrayList<CorefTarget>> spans2) {
        for (int i = 0; i < spans1.size(); i++) {
            ArrayList<CorefTarget> corefTargets1 = spans1.get(i);
            return matchingAtLeastOneSetOfSpans(corefTargets1, spans2);
        }
        return false;
    }

    /**
     * Counts intersecting spans across lists of CorefTargets
     * @param spans1
     * @param spans2
     * @return
     */
    static public int countIntersectingSetOfSpans (ArrayList<ArrayList<CorefTarget>> spans1, ArrayList<ArrayList<CorefTarget>> spans2) {
        int cnt = 0;
        for (int i = 0; i < spans1.size(); i++) {
            ArrayList<CorefTarget> corefTargets1 = spans1.get(i);
            if (matchingAtLeastOneSetOfSpans(corefTargets1, spans2)) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     *
     * @param corefTargets1
     * @param spans2
     * @return
     */
    static public boolean matchingAtLeastOneSetOfSpans (ArrayList<CorefTarget> corefTargets1, ArrayList<ArrayList<CorefTarget>> spans2) {
        for (int k = 0; k < spans2.size(); k++) {
            ArrayList<CorefTarget> corefTargets2 = spans2.get(k);
            //// for each set we check if there is a full match
            if (matchAllSpans(corefTargets1, corefTargets2)) {
                //// there is one span set with a full match
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param corefTargets1
     * @param spans2
     * @return
     */
    static public boolean intersectingWithAtLeastOneSetOfSpans (ArrayList<CorefTarget> corefTargets1, ArrayList<ArrayList<CorefTarget>> spans2) {
        for (int k = 0; k < spans2.size(); k++) {
            ArrayList<CorefTarget> corefTargets2 = spans2.get(k);
            //// for each set we check if there is a sufficient match
            if (intersectingThresholdSpans(corefTargets1, corefTargets2)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param spans1
     * @param spans2
     * @return
     */
    static public boolean matchingAllSpansForOneSpanSet (ArrayList<ArrayList<CorefTarget>> spans1, ArrayList<ArrayList<CorefTarget>> spans2) {
        for (int i = 0; i < spans1.size(); i++) {
            ArrayList<CorefTarget> corefTargets1 = spans1.get(i);
            for (int k = 0; k < spans2.size(); k++) {
                ArrayList<CorefTarget> corefTargets2 = spans2.get(k);
                if (matchAllSpans(corefTargets1, corefTargets2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param spans1
     * @param spans2
     * @return
     */
    static public boolean matchAllSpans(ArrayList<CorefTarget> spans1, ArrayList<CorefTarget> spans2) {
        for (int i = 0; i < spans1.size(); i++) {
            CorefTarget span1 = spans1.get(i);
            for (int j = 0; j < spans2.size(); j++) {
                CorefTarget span2 =  spans2.get(j);
                if (!span1.getId().equals(span2.getId())) {
                    return false;
                }
            }
        }
        return true;
    }

    static public boolean intersectingSpans(ArrayList<CorefTarget> spans1, ArrayList<CorefTarget> spans2) {
        for (int i = 0; i < spans1.size(); i++) {
            CorefTarget span1 = spans1.get(i);
            for (int j = 0; j < spans2.size(); j++) {
                CorefTarget span2 =  spans2.get(j);
                if (span1.getId().equals(span2.getId())) {
                    return true;
                }
            }
        }
        return true;
    }


    static public boolean intersectingThresholdSpans(ArrayList<CorefTarget> spans1, ArrayList<CorefTarget> spans2) {
        int matchCount = 0;
        for (int i = 0; i < spans1.size(); i++) {
            CorefTarget span1 = spans1.get(i);
            for (int j = 0; j < spans2.size(); j++) {
                CorefTarget span2 =  spans2.get(j);
                if (span1.getId().equals(span2.getId())) {
                    matchCount++;
                    break;
                }
            }
        }
        int span1MatchScore = ((matchCount * 100) / spans1.size());
        int span2MatchScore = ((matchCount * 100) / spans2.size());
        int matchScoreAverage = (span1MatchScore + span2MatchScore) / 2;
        if (matchScoreAverage >= SPANMATCHTHRESHOLD) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param spans
     * @param semObject
     * @return
     */
    static public boolean matchAtLeastASingleSpan(ArrayList<String> spans, SemObject semObject) {
        for (int i = 0; i < semObject.getNafMentions().size(); i++) {
            ArrayList<NafMention> mentions = semObject.getNafMentions();
            for (int j = 0; j < mentions.size(); j++) {
                NafMention nafMention = mentions.get(j);
                for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                    String id = nafMention.getTermsIds().get(k);
                    if (spans.contains(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @param spans
     * @param semObject
     * @return
     */
    static public boolean matchAllSpans(ArrayList<String> spans, SemObject semObject) {
        for (int i = 0; i < semObject.getNafMentions().size(); i++) {
            ArrayList<NafMention> mentions = semObject.getNafMentions();
            for (int j = 0; j < mentions.size(); j++) {
                NafMention nafMention = mentions.get(j);
                for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                    String id = nafMention.getTermsIds().get(k);
                    if (!spans.contains(id)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static public boolean containsAllSpans(ArrayList<String> spans, SemObject semObject) {
        boolean match = false;
        for (int i = 0; i < semObject.getNafMentions().size(); i++) {
            ArrayList<NafMention> mentions = semObject.getNafMentions();
            for (int j = 0; j < mentions.size(); j++) {
                NafMention nafMention = mentions.get(j);
                match = false;
                for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                    String id = nafMention.getTermsIds().get(k);
                    if (!spans.contains(id)) {
                        match = false;
                        break;
                    }
                    else {
                        match = true;
                    }
                }
                if (match) {
                    break;
                }
                else {
                    //try next one
                }
            }
        }
        return match;
    }

    /**
     *
     * @param spans
     * @param semObject
     * @return
     */
    static public boolean matchAllOfAnyMentionSpans(ArrayList<String> spans, SemObject semObject) {
        for (int i = 0; i < semObject.getNafMentions().size(); i++) {
            ArrayList<NafMention> mentions = semObject.getNafMentions();
            for (int j = 0; j < mentions.size(); j++) {
                NafMention nafMention = mentions.get(j);
                boolean localmatch = true;
                for (int k = 0; k < spans.size(); k++) {
                    String spanId = spans.get(k);
                    if (!nafMention.getTermsIds().contains(spanId))  {
                        localmatch = false;
                        break;
                    }
                }
                if (localmatch) {
                    /// we found a perfect match
                    return true;
                }
            }
        }
        return false;
    }

    static public String getBestEntityUri (KafEntity kafEntity) {
        String uri="";
        boolean RERANK = false;
        for (int i = 0; i < kafEntity.getExternalReferences().size(); i++) {
            KafSense kafSense = kafEntity.getExternalReferences().get(i);
            if (kafSense.getResource().toLowerCase().startsWith("vua-type-reranker")) {
                uri = kafSense.getSensecode();
                RERANK = true;
                break;
            }
        }
        if (!RERANK) {
            uri = kafEntity.getFirstUriReference();
        }
        return uri;
    }

    static public String getEntityLabelUriFromEntities (ArrayList<KafEntity> entities) {
        String uri = "";
        SemObject semObject = new SemObject();
        for (int i = 0; i < entities.size(); i++) {
            KafEntity kafEntity = entities.get(i);
            String aUri = null;
            try {
                String cleanLabel = kafEntity.getTokenString().replaceAll(" ", "_");
                //cleanLabel = cleanLabel.replaceAll("\'", "");
                aUri = URLEncoder.encode(cleanLabel, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (aUri!=null) {
                semObject.addPhraseCounts(aUri);
            }
        }
        uri = semObject.getTopPhraseAsLabel();
        return uri;
    }
    /**
     * Returns the span id that is the head of a constituent of a participant in the SRL layer
     * @param kafParticipant
     * @return
     */
    static public String getHeadId (KafParticipant kafParticipant) {
        for (int i = 0; i < kafParticipant.getSpans().size(); i++) {
            CorefTarget corefTarget = kafParticipant.getSpans().get(i);
            if (!corefTarget.getHead().isEmpty()) {
                return corefTarget.getId();
            }
        }
        return "";
    }


    /**
     * Compares all SemObjects with a KafParticipant from the SRL layer to return the object that has a mention with the largest span overlap.
     * If none of the objects exceeds or equal to the SPANMATCHTHRESHOLD, null is returned
     *
     * KafParticipants (roles in the SRL) have span with a head attribute.
     * However, very often the preposition or relative clause complement is marked as the head
     * while these are never part of the entity or coreferece span. We therefore ignore the head attribute
     * and only consider the content words.
     * WE COUNT OVERLAP FOR POS=N,V,A,G ONLY
     *
     * @param kafSaxParser
     * @param kafParticipant
     * @param semObjects
     * @return
     */
    static public SemObject getBestMatchingObject(KafSaxParser kafSaxParser,
                                                  KafParticipant kafParticipant,
                                                  ArrayList<SemObject> semObjects) {

        boolean DEBUG = false;
        SemObject topObject = null;
        int topScore = 0;
/*
        if (kafParticipant.getId().equals("rl19")) {
            DEBUG = true;
        }
*/
        int nContentWordsKafParticipant = kafSaxParser.getNumberContentWords(kafParticipant.getSpanIds());
        for (int i = 0; i < semObjects.size(); i++) {
            SemObject semObject = semObjects.get(i);
            for (int m = 0; m < semObject.getNafMentions().size(); m++) {
                // FOR EVERY MENTION, WE CHECK THE OVERLAP WITH THE KAFPARTICIPANT AND KEEP THE BEST
                NafMention nafMention = semObject.getNafMentions().get(m);
                int matchCount = 0;
                int nContentWordsNafMention = 0;
                for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                    String id = nafMention.getTermsIds().get(k);
                    if (kafSaxParser.contentWord(id)) {
                        nContentWordsNafMention++;
                        if (kafParticipant.getSpanIds().contains(id)) {
                            matchCount++;
                        }
                    }
                }
                if ((nContentWordsNafMention>0) && (nContentWordsKafParticipant>0)) {
                    int matchScoreObject = ((matchCount * 100) / nContentWordsNafMention);
                    int matchScoreParticipant = ((matchCount * 100) / nContentWordsKafParticipant);
                    int matchScoreAverage = (matchScoreObject + matchScoreParticipant) / 2;
                    if (matchScoreAverage>0) {
/*                        System.out.println("matchScoreParticipant = " + matchScoreParticipant);
                        System.out.println("matchScoreObject = " + matchScoreObject);
                        System.out.println("matchScoreAverage = " + matchScoreAverage);
                        System.out.println("semObject = " + semObject.getTopPhraseAsLabel());
                        System.out.println("kafParticipant = " + kafParticipant.getTokenString());*/
                        if (matchScoreAverage >= SPANMATCHTHRESHOLD) {
                            if (matchScoreAverage > topScore) {
                                topScore = matchScoreAverage;
                                topObject = semObject;
                            }
                        }
                    }
                }
            }
        }

        return topObject;
    }



    static public ArrayList<SemObject> getAllMatchingObject(KafSaxParser kafSaxParser,
                                                  KafParticipant kafParticipant,
                                                  ArrayList<SemObject> semObjects) {

        ArrayList<SemObject> topObjects = new ArrayList<SemObject>();
        int nContentWordsKafParticipant = kafSaxParser.getNumberContentWords(kafParticipant.getSpanIds());
        for (int i = 0; i < semObjects.size(); i++) {
            SemObject semObject = semObjects.get(i);
            for (int m = 0; m < semObject.getNafMentions().size(); m++) {
                // FOR EVERY MENTION, WE CHECK THE OVERLAP WITH THE KAFPARTICIPANT AND KEEP THE ONE ABOVE THE SPANMATCHTHRESHOLD
                NafMention nafMention = semObject.getNafMentions().get(m);
                int matchCount = 0;
                int nContentWordsNafMention = 0;
                for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                    String id = nafMention.getTermsIds().get(k);
                    if (kafSaxParser.contentWord(id)) {
                        nContentWordsNafMention++;
                        if (kafParticipant.getSpanIds().contains(id)) {
                            matchCount++;
                        }
                    }
                }
                if ((nContentWordsNafMention>0) && (nContentWordsKafParticipant>0)) {
                    int matchScoreObject = ((matchCount * 100) / nContentWordsNafMention);
                    int matchScoreParticipant = ((matchCount * 100) / nContentWordsKafParticipant);
                    int matchScoreAverage = (matchScoreObject + matchScoreParticipant) / 2;
                    if (matchScoreAverage>0) {
/*                        System.out.println("matchScoreParticipant = " + matchScoreParticipant);
                        System.out.println("matchScoreObject = " + matchScoreObject);
                        System.out.println("matchScoreAverage = " + matchScoreAverage);
                        System.out.println("semObject = " + semObject.getTopPhraseAsLabel());
                        System.out.println("kafParticipant = " + kafParticipant.getTokenString());*/
                        if (matchScoreAverage >= SPANMATCHTHRESHOLD) {
                            topObjects.add(semObject);
                        }
                    }
                }
            }
        }

        return topObjects;
    }

    /**
     * @Deprecated
     * This function tries to find the head match and either a full match or a partial match.
     * Replaced by more simple and straight forward function: getBestMatchingObject
     * @param kafSaxParser
     * @param kafParticipant
     * @param semObject
     * @return
     */
    static public boolean matchAllSpansOfAnObjectMentionOrTheRoleHead(KafSaxParser kafSaxParser,
                                                                      KafParticipant kafParticipant,
                                                                      SemObject semObject) {
        String headSpan = "";
        boolean functionWordPos = false;
        for (int i = 0; i < kafParticipant.getSpans().size(); i++) {
            CorefTarget corefTarget = kafParticipant.getSpans().get(i);
            if (!corefTarget.getHead().isEmpty()) {

               ///// A1, A2, A3, A4 and AM often have a preposition as the head which is mostly not part of the semObject mention
               ///// to be sure the head is content word, we check the POS of the term
                KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
                if (kafTerm!=null) {
                    if (kafTerm.getPos().toLowerCase().startsWith("p.") || kafTerm.getPos().equalsIgnoreCase("p")) {
                        /// not a content word so cannot match a semObject span
                        functionWordPos = true;
                       // System.out.println("kafTerm.getTid() = " + kafTerm.getTid());
                    }
                    else {
                        headSpan = corefTarget.getId();
                    }
                }
                else {
                  //  System.out.println("Could not find the term:"+corefTarget.getId());
                }
                break;
            }
        }

            // System.out.println("headSpan = " + headSpan);
        ////// Since the head match is empty we need to know if all the mentions in the semObject are part of the span of the role
        if (headSpan.isEmpty()) {
            boolean fullmatch = true;
            boolean submatch = false;
            for (int i = 0; i < semObject.getNafMentions().size(); i++) {
                    NafMention nafMention = semObject.getNafMentions().get(i);
                    for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                        String id = nafMention.getTermsIds().get(k);
                        if (kafParticipant.getSpanIds().contains(id)) {
                            submatch = true;
                        }
                        else {
                            ///this one is falsified
                            fullmatch = false;
                        }
                    }
            }

            if (fullmatch) {
                /// we found all the terms of the semObject mention
                /// this is the minimal requirements
                return true;
            }
            else if (functionWordPos && submatch) {
                    return true;
            }
            else {
                /// we did not find any full local match so we return false
                /// if we get here we know there is no match
                //// to soften this submatch is used but this is risky especially for longer spans of roles (can be complete sentences!)
                return false;
            }
        }
        else {
            ///// if the head span matches with any of the object spans this is sufficient

            /// If the span matches this is sufficient.
            for (int i = 0; i < semObject.getNafMentions().size(); i++) {
                ArrayList<NafMention> mentions = semObject.getNafMentions();
                for (int j = 0; j < mentions.size(); j++) {
                    NafMention nafMention = mentions.get(j);
                    for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                        String id = nafMention.getTermsIds().get(k);
                        if (headSpan.equals(id)) {
                            //    System.out.println("nafMention = " + nafMention.toString());
                            //    System.out.println("id = " + id);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }


    static public void setFactuality(KafSaxParser kafSaxParser, NafMention nafMention) {
        for (int i = 0; i < kafSaxParser.kafFactualityLayer.size(); i++) {
            KafFactuality kafFactuality = kafSaxParser.kafFactualityLayer.get(i);
            if (nafMention.getTokensIds().contains(kafFactuality.getId())) {
                //   System.out.println("nafMention.toString() = " + nafMention.toString());
                nafMention.setFactuality(kafFactuality);
            }
        }
    }

    /**
     * Checks if two SemObject have a mention in the same sentence
     * @param kafSaxParser
     * @param semObject1
     * @param semObject2
     * @return
     */
    static public boolean sameSentence(KafSaxParser kafSaxParser, SemObject semObject1, SemObject semObject2) {
        for (int i = 0; i < semObject1.getNafMentions().size(); i++) {
            NafMention nafMention1 = semObject1.getNafMentions().get(i);
            for (int j = 0; j < nafMention1.getTokensIds().size(); j++) {
                String tokenId = nafMention1.getTokensIds().get(j);
                KafWordForm kafWordForm1 = kafSaxParser.getWordForm(tokenId);
                for (int k = 0; k < semObject2.getNafMentions().size(); k++) {
                    NafMention nafMention2 = semObject2.getNafMentions().get(k);
                    for (int l = 0; l < nafMention2.getTokensIds().size(); l++) {
                        String tokenId2 = nafMention2.getTokensIds().get(l);
                        KafWordForm kafWordForm2 = kafSaxParser.getWordForm(tokenId2);
                        if (kafWordForm1.getSent().equals(kafWordForm2.getSent())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if two SemObject have a mention in the same sentence
     * @param semObject1
     * @param semObject2
     * @return
     */
    static public ArrayList<String> sameSentenceRange(SemObject semObject1, SemObject semObject2) {
        ArrayList<String> ids = new ArrayList<String>();
        for (int i = 0; i < semObject1.getNafMentions().size(); i++) {
            NafMention nafMention1 = semObject1.getNafMentions().get(i);
            for (int k = 0; k < semObject2.getNafMentions().size(); k++) {
                NafMention nafMention2 = semObject2.getNafMentions().get(k);
                if (!nafMention2.getSentence().isEmpty()) {
                    if (nafMention1.getSentence().equals(nafMention2.getSentence())) {
                        //if (semObject2.getId().endsWith("tmx7")) System.out.println(nafMention1.getSentence()+":" + nafMention2.getSentence());
                        for (int m = 0; m < nafMention1.getTermsIds().size(); m++) {
                            String id = nafMention1.getTermsIds().get(m);
                            if (!ids.contains(id)) {
                                ids.add(id);
                            }
                        }
                        for (int m = 0; m < nafMention2.getTermsIds().size(); m++) {
                            String id = nafMention2.getTermsIds().get(m);
                            if (!ids.contains(id)) {
                                ids.add(id);
                            }
                        }
                        break;
                    }
                }
            }
        }
        return ids;
    }

    /**
     * Checks if two SemObject have a mention within 4 sentences span 2 before and 1 after
     * @param semObject1
     * @param semObject2
     * @return
     */
    static public ArrayList<String> range1SentenceRange(SemObject semObject1, SemObject semObject2) {
        ArrayList<String> ids = new ArrayList<String>();
        for (int i = 0; i < semObject1.getNafMentions().size(); i++) {
            NafMention nafMention1 = semObject1.getNafMentions().get(i);
            for (int k = 0; k < semObject2.getNafMentions().size(); k++) {
                NafMention nafMention2 = semObject2.getNafMentions().get(k);
                if ((!nafMention1.getSentence().isEmpty()) &&
                        (!nafMention2.getSentence().isEmpty()))    {
                    try {
                        int s1 = 0;
                        try {
                            s1 = Integer.parseInt(nafMention1.getSentence());
                            int s2 = 0;
                            try {
                                s2 = Integer.parseInt(nafMention2.getSentence());
                                if (s1 - 1 == s2 || s1 + 1 == s2) {
                                   // if (semObject2.getId().endsWith("tmx12")) System.out.println(s1+":"+s2);
                                    for (int m = 0; m < nafMention1.getTermsIds().size(); m++) {
                                        String id = nafMention1.getTermsIds().get(m);
                                        if (!ids.contains(id)) {
                                            ids.add(id);
                                        }
                                    }
                                    for (int m = 0; m < nafMention2.getTermsIds().size(); m++) {
                                        String id = nafMention2.getTermsIds().get(m);
                                        if (!ids.contains(id)) {
                                            ids.add(id);
                                        }
                                    }
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                /// the document creation time is presented as a timex without span
                                /// this results in empty sentence and an error
                                    e.printStackTrace();
                            }
                        } catch (NumberFormatException e) {
                              e.printStackTrace();
                        }
                    } catch (NumberFormatException e) {
                         e.printStackTrace();
                    }
                }
            }
        }
        return ids;
    }

    /**
     * Checks if two SemObject have a mention within 4 sentences span 2 before and 1 after
     * @param semObject1
     * @param semObject2
     * @return
     */
    static public ArrayList<String> rangemin2plus1SentenceRange(SemObject semObject1, SemObject semObject2) {
        ArrayList<String> ids = new ArrayList<String>();
        for (int i = 0; i < semObject1.getNafMentions().size(); i++) {
            NafMention nafMention1 = semObject1.getNafMentions().get(i);
            for (int k = 0; k < semObject2.getNafMentions().size(); k++) {
                NafMention nafMention2 = semObject2.getNafMentions().get(k);
                if ((!nafMention1.getSentence().isEmpty()) &&
                        (!nafMention2.getSentence().isEmpty()))    {
                    try {
                        int s1 = 0;
                        try {
                            Integer.parseInt(nafMention1.getSentence());
                            int s2 = 0;
                            try {
                                Integer.parseInt(nafMention2.getSentence());
                                if (s1 - 2 == s2 || s1 + 1 == s2) {
                                   // if (semObject2.getId().endsWith("tmx12"))
                                   // System.out.println(s1+":"+s2);
                                    for (int m = 0; m < nafMention1.getTermsIds().size(); m++) {
                                        String id = nafMention1.getTermsIds().get(m);
                                        if (!ids.contains(id)) {
                                            ids.add(id);
                                        }
                                    }
                                    for (int m = 0; m < nafMention2.getTermsIds().size(); m++) {
                                        String id = nafMention2.getTermsIds().get(m);
                                        if (!ids.contains(id)) {
                                            ids.add(id);
                                        }
                                    }
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // e.printStackTrace();
                            }

                        } catch (NumberFormatException e) {
                            // e.printStackTrace();
                        }
                    } catch (NumberFormatException e) {
                        // e.printStackTrace();
                    }
                }
            }
        }
        return ids;
    }

    /**
     * Checks if two SemObject have a mention within 4 sentences span 2 before and 1 after
     * @param kafSaxParser
     * @param semObject1
     * @param semObject2
     * @return
     */
    static public boolean rangemin2plus1Sentence(KafSaxParser kafSaxParser, SemObject semObject1, SemObject semObject2) {
        for (int i = 0; i < semObject1.getNafMentions().size(); i++) {
            NafMention nafMention1 = semObject1.getNafMentions().get(i);
            for (int j = 0; j < nafMention1.getTokensIds().size(); j++) {
                String tokenId = nafMention1.getTokensIds().get(j);
                KafWordForm kafWordForm1 = kafSaxParser.getWordForm(tokenId);
                for (int k = 0; k < semObject2.getNafMentions().size(); k++) {
                    NafMention nafMention2 = semObject2.getNafMentions().get(k);
                    if ((!nafMention1.getSentence().isEmpty()) &&
                            (!nafMention2.getSentence().isEmpty()))    {                        for (int l = 0; l < nafMention2.getTokensIds().size(); l++) {
                            String tokenId2 = nafMention2.getTokensIds().get(l);
                            KafWordForm kafWordForm2 = kafSaxParser.getWordForm(tokenId2);
                            if (kafWordForm1.getSent().equals(kafWordForm2.getSent())) {
                                return true;
                            }
                            try {
                                int s1 = 0;
                                try {
                                    Integer.parseInt(kafWordForm1.getSent());
                                    int s2 = 0;
                                    try {
                                        Integer.parseInt(kafWordForm2.getSent());
                                        if (s1 == s2 || s1 - 1 == s2 || s1 - 2 == s2 || s1 + 1 == s2) {
                                            return true;
                                        }
                                    } catch (NumberFormatException e) {
                                        // e.printStackTrace();
                                    }
                                } catch (NumberFormatException e) {
                                    // e.printStackTrace();
                                }
                            } catch (NumberFormatException e) {
                                // e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if two mentions within 4 sentences span 2 before and 1 after
     * @param kafSaxParser
     * @param semObject1
     * @param nafMention2
     * @return
     */
    static public boolean rangemin2plus1Sentence(KafSaxParser kafSaxParser, SemObject semObject1, NafMention nafMention2) {
        for (int i = 0; i < semObject1.getNafMentions().size(); i++) {
            NafMention nafMention1 = semObject1.getNafMentions().get(i);
            for (int j = 0; j < nafMention1.getTokensIds().size(); j++) {
                String tokenId = nafMention1.getTokensIds().get(j);
                KafWordForm kafWordForm1 = kafSaxParser.getWordForm(tokenId);
                for (int l = 0; l < nafMention2.getTokensIds().size(); l++) {
                    String tokenId2 = nafMention2.getTokensIds().get(l);
                    KafWordForm kafWordForm2 = kafSaxParser.getWordForm(tokenId2);
                    if (kafWordForm1.getSent().equals(kafWordForm2.getSent())) {
                        return true;
                    }
                    try {
                        int s1 = 0;
                        try {
                            Integer.parseInt(kafWordForm1.getSent());
                            int s2 = 0;
                            try {
                                Integer.parseInt(kafWordForm2.getSent());
                                if (s1==s2 || s1-1==s2 || s1-2==s2 || s1+1==s2) {
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                //  e.printStackTrace();
                            }
                        } catch (NumberFormatException e) {
                           // e.printStackTrace();
                        }
                    } catch (NumberFormatException e) {
                       // e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if two SemObject have a mention within 4 sentences span 2 before and 1 after
     * @param kafSaxParser
     * @param semObject1
     * @param semObject2
     * @return
     */
    static public boolean rangemin5Sentence(KafSaxParser kafSaxParser, SemObject semObject1, SemObject semObject2) {
        for (int i = 0; i < semObject1.getNafMentions().size(); i++) {
            NafMention nafMention1 = semObject1.getNafMentions().get(i);
            for (int j = 0; j < nafMention1.getTokensIds().size(); j++) {
                String tokenId = nafMention1.getTokensIds().get(j);
                KafWordForm kafWordForm1 = kafSaxParser.getWordForm(tokenId);
                for (int k = 0; k < semObject2.getNafMentions().size(); k++) {
                    NafMention nafMention2 = semObject2.getNafMentions().get(k);
                    if (!nafMention2.getSentence().isEmpty()) {
                        for (int l = 0; l < nafMention2.getTokensIds().size(); l++) {
                            String tokenId2 = nafMention2.getTokensIds().get(l);
                            KafWordForm kafWordForm2 = kafSaxParser.getWordForm(tokenId2);
                            if (kafWordForm1.getSent().equals(kafWordForm2.getSent())) {
                                return true;
                            }
                            try {
                                int s1 = 0;
                                try {
                                    Integer.parseInt(kafWordForm1.getSent());
                                } catch (NumberFormatException e) {
                                    // e.printStackTrace();
                                }
                                int s2 = 0;
                                try {
                                    Integer.parseInt(kafWordForm2.getSent());
                                } catch (NumberFormatException e) {
                                    //e.printStackTrace();
                                }
                                if (s1 == s2 || s1 - 1 == s2 || s1 - 2 == s2 || s1 - 3 == s2 || s1 - 4 == s2 || s1 - 5 == s2) {
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                // e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    /////////////////////////////////////////////////////////////////////////
    //////////
    //////////
    //////////
    //////////


    static public ArrayList<NafMention> getNafMentionArrayList (String baseUri,
                                                                KafSaxParser kafSaxParser,
                                                                ArrayList<ArrayList<CorefTarget>> corefTargetArrayList) {
        ArrayList<NafMention> mentionURIs = new ArrayList<NafMention>();
        for (int i = 0; i < corefTargetArrayList.size(); i++) {
            ArrayList<CorefTarget> corefTargets = corefTargetArrayList.get(i);
            //System.out.println("corefTargets.toString() = " + corefTargets.toString());
            NafMention mention = getNafMentionForCorefTargets(baseUri, kafSaxParser, corefTargets);
            if (!hasMention(mentionURIs, mention)) {
                mentionURIs.add(mention);
            }
        }
        return mentionURIs;
    }

    static public ArrayList<NafMention> getNafMentionArrayListForTermIds (String baseUri,
                                                                KafSaxParser kafSaxParser,
                                                                ArrayList<String> termIds) {
        ArrayList<NafMention> mentionURIs = new ArrayList<NafMention>();
        for (int i = 0; i < termIds.size(); i++) {
            String termId = termIds.get(i);
            NafMention mention = getNafMentionForTermId(baseUri, kafSaxParser, termId);
            if (!hasMention(mentionURIs, mention)) {
                mentionURIs.add(mention);
            }
        }
        return mentionURIs;
    }

    static public ArrayList<NafMention> getNafMentionArrayListFromEntitiesAndCoreferences (String baseUri,
                                                                                           KafSaxParser kafSaxParser,
                                                                                           ArrayList<KafEntity> kafEntities) {
        ArrayList<NafMention> mentionURIs = new ArrayList<NafMention>();
        for (int i = 0; i < kafEntities.size(); i++) {
            KafEntity kafEntity = kafEntities.get(i);
            ArrayList<ArrayList<CorefTarget>> corefTargetSets = kafEntity.getSetsOfSpans();
            ArrayList<ArrayList<CorefTarget>> sets = getCorefTargetSetsForEntitySpans(corefTargetSets, kafSaxParser.kafCorefenceArrayList);
            for (int j = 0; j < sets.size(); j++) {
                ArrayList<CorefTarget> corefTargets = sets.get(j);
                NafMention mention = getNafMentionForCorefTargets(baseUri, kafSaxParser, corefTargets);
                if (!hasMention(mentionURIs, mention)) {
                   // System.out.println("corefTargets.toString() = " + corefTargets.toString());
                    mentionURIs.add(mention);
                }
            }
        }
        return mentionURIs;
    }

    static public ArrayList<NafMention> getNafMentionArrayListFromEntities (String baseUri,
                                                                                           KafSaxParser kafSaxParser,
                                                                                           ArrayList<KafEntity> kafEntities) {
        ArrayList<NafMention> mentionURIs = new ArrayList<NafMention>();
        for (int i = 0; i < kafEntities.size(); i++) {
            KafEntity kafEntity = kafEntities.get(i);
            ArrayList<ArrayList<CorefTarget>> corefTargetSets = kafEntity.getSetsOfSpans();
            for (int j = 0; j < corefTargetSets.size(); j++) {
                ArrayList<CorefTarget> corefTargets = corefTargetSets.get(j);
                NafMention mention = getNafMentionForCorefTargets(baseUri, kafSaxParser, corefTargets);
                if (!hasMention(mentionURIs, mention)) {
                    mentionURIs.add(mention);
                }
            }
        }
        return mentionURIs;
    }

    static public ArrayList<NafMention> getNafMentionArrayListFromPredicatesAndCoreferences (String baseUri,
                                                                                           KafSaxParser kafSaxParser,
                                                                                           KafEvent kafEvent) {
        ArrayList<NafMention> mentionURIs = new ArrayList<NafMention>();
        ArrayList<CorefTarget> corefTargetSets = kafEvent.getSpans();
        ArrayList<ArrayList<CorefTarget>> sets = getCorefTargetSetsForEventSpans(corefTargetSets, kafSaxParser.kafCorefenceArrayList);
        for (int j = 0; j < sets.size(); j++) {
            ArrayList<CorefTarget> corefTargets = sets.get(j);
            NafMention mention = getNafMentionForCorefTargets(baseUri, kafSaxParser, corefTargets);
            if (!hasMention(mentionURIs, mention)) {
               // System.out.println("corefTargets.toString() = " + corefTargets.toString());
                mentionURIs.add(mention);
            }
        }
        return mentionURIs;
    }

    static public boolean hasMention (ArrayList<NafMention> mentions, NafMention nafMention) {
        for (int i = 0; i < mentions.size(); i++) {
            NafMention mention = mentions.get(i);
            if (mention.toString().equals(nafMention.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     *      Mention URI = News URI + "#char=START_OFFSET,END_OFFSET"
     * @param kafSaxParser
     * @param corefTargets
     */
    static public NafMention getNafMentionForCorefTargets (String baseUri, KafSaxParser kafSaxParser, ArrayList<CorefTarget> corefTargets) {
        NafMention mention = new NafMention();
        int firstOffSet = -1;
        int highestOffSet = -1;
        int lengthOffSet = -1;
        mention.setBaseUri(baseUri);
        for (int j = 0; j < corefTargets.size(); j++) {
            CorefTarget corefTarget = corefTargets.get(j);
            KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
            mention.addTermsId(corefTarget.getId());
            if (kafTerm==null) {
                // System.out.println("corefTarget = " + corefTarget.getId());
            }
            else {
                for (int i = 0; i < kafTerm.getSpans().size(); i++) {
                    String tokenId = kafTerm.getSpans().get(i);
                    KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                    if (kafWordForm==null) {
                        continue;
                    }
                    mention.addTokensId(kafWordForm.getWid());
                    if (!kafWordForm.getCharOffset().isEmpty()) {
                        int offSet = 0;
                        try {
                            offSet = Integer.parseInt(kafWordForm.getCharOffset());
                        } catch (NumberFormatException e) {
                           // e.printStackTrace();
                        }
                        int length = 0;
                        try {
                            length = Integer.parseInt(kafWordForm.getCharLength());
                        } catch (NumberFormatException e) {
                        //    e.printStackTrace();
                        }
                        if (length==0) {
                         length = kafWordForm.getWf().length();
                        }
                        if (firstOffSet==-1 || firstOffSet>offSet) {
                            firstOffSet = offSet;
                        }
                        if (highestOffSet==-1 ||offSet>highestOffSet) {
                            highestOffSet = offSet;
                            lengthOffSet = length;
                        }
                    }
                }
            }
        }
        if (firstOffSet>-1 && highestOffSet>-1) {
            int end_offset = highestOffSet+lengthOffSet;
            mention.setOffSetStart(new Integer (firstOffSet).toString());
            mention.setOffSetEnd(new Integer(end_offset).toString());
        }
        mention.setSentence(kafSaxParser);
        mention.setPhraseFromMention(kafSaxParser);
        return mention;
    }


    static public NafMention getNafMentionForTermId (String baseUri, KafSaxParser kafSaxParser, String termId) {
        NafMention mention = new NafMention();
        mention.setBaseUri(baseUri);
        int firstOffSet = -1;
        int highestOffSet = -1;
        int lengthOffSet = -1;
        mention.setBaseUri(baseUri);
        mention.addTermsId(termId);
        KafTerm kafTerm = kafSaxParser.getTerm(termId);

        if (kafTerm==null) {
            // System.out.println("corefTarget = " + corefTarget.getId());
        }
        else {
            for (int i = 0; i < kafTerm.getSpans().size(); i++) {
                String tokenId = kafTerm.getSpans().get(i);
                KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                mention.addTokensId(kafWordForm.getWid());
                if (!kafWordForm.getCharOffset().isEmpty()) {
                    int offSet = 0;
                    try {
                        offSet = Integer.parseInt(kafWordForm.getCharOffset());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    int length = 0;
                    try {
                        length = Integer.parseInt(kafWordForm.getCharLength());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (firstOffSet==-1 || firstOffSet>offSet) {
                        firstOffSet = offSet;
                    }
                    if (highestOffSet==-1 ||offSet>highestOffSet) {
                        highestOffSet = offSet;
                        lengthOffSet = length;
                    }
                }
            }
        }
        if (firstOffSet>-1 && highestOffSet>-1) {
            int end_offset = highestOffSet+lengthOffSet;
            mention.setOffSetStart(new Integer (firstOffSet).toString());
            mention.setOffSetEnd(new Integer(end_offset).toString());
        }
        mention.setSentence(kafSaxParser);
        mention.setPhraseFromMention(kafSaxParser);
        return mention;
    }

    static public NafMention getNafMentionForTermIdArrayList (String baseUri,
                                                              KafSaxParser kafSaxParser,
                                                              ArrayList<String> termIds) {
        NafMention mention = new NafMention();
        mention.setBaseUri(baseUri);
        int firstOffSet = -1;
        int highestOffSet = -1;
        int lengthOffSet = -1;
        mention.setBaseUri(baseUri);
        for (int j = 0; j < termIds.size(); j++) {
            String termId = termIds.get(j);
            KafTerm kafTerm = kafSaxParser.getTerm(termId);
            mention.addTermsId(termId);
            if (kafTerm==null) {
                // System.out.println("corefTarget = " + corefTarget.getId());
            }
            else {
                for (int i = 0; i < kafTerm.getSpans().size(); i++) {
                    String tokenId = kafTerm.getSpans().get(i);
                    KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                    mention.addTokensId(kafWordForm.getWid());
                    if (!kafWordForm.getCharOffset().isEmpty()) {
                        int offSet = 0;
                        try {
                            offSet = Integer.parseInt(kafWordForm.getCharOffset());
                        } catch (NumberFormatException e) {
                            //e.printStackTrace();
                        }
                        int length = 0;
                        try {
                            length = Integer.parseInt(kafWordForm.getCharLength());
                        } catch (NumberFormatException e) {
                            //    e.printStackTrace();
                        }
                        if (length==0) {
                            length = kafWordForm.getWf().length();
                        }
                        if (firstOffSet==-1 || firstOffSet>offSet) {
                            firstOffSet = offSet;
                        }
                        if (highestOffSet==-1 ||offSet>highestOffSet) {
                            highestOffSet = offSet;
                            lengthOffSet = length;
                        }
                    }
                }
            }
        }
        if (firstOffSet>-1 && highestOffSet>-1) {
            int end_offset = highestOffSet+lengthOffSet;
            mention.setOffSetStart(new Integer (firstOffSet).toString());
            mention.setOffSetEnd(new Integer(end_offset).toString());
        }
        mention.setSentence(kafSaxParser);
        mention.setPhraseFromMention(kafSaxParser);
        return mention;
    }

    static public ArrayList<String> getPredicateIdsForNafMentions (ArrayList<NafMention> nafMentions, KafSaxParser kafSaxParser) {
        ArrayList<String> predicateIds = new ArrayList<String>();
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < nafMentions.size(); j++) {
                NafMention nafMention = nafMentions.get(j);
                for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                    String termId =  nafMention.getTermsIds().get(k);
                    if (kafEvent.getSpanIds().contains(termId)) {
                        if (!predicateIds.contains(kafEvent.getId())) {
                            predicateIds.add(kafEvent.getId());
                        }
                        break;
                    }
                }
            }
        }
        return predicateIds;
    }

    static public boolean matchTimeReference (ArrayList<SemTime> times1, ArrayList<SemTime> times2, String time1Id, String time2Id) {
        SemTime semTime1 = null;
        SemTime semTime2 = null;
        if (time1Id.equals(time2Id)) {
            return true;
        }
        for (int i = 0; i < times1.size(); i++) {
            SemTime aTime = times1.get(i);
          //  System.out.println("aTime = " + aTime);
          //  System.out.println("time1Id = " + time1Id);
            if (aTime.getId().equals(time1Id)) {
              //  System.out.println("aTime.toString() = " + aTime.toString());
                semTime1 = aTime;
                break;
            }
        }
        for (int j = 0; j < times2.size(); j++) {
            SemTime aTime =  times2.get(j);
            if (aTime.getId().equals(time2Id)) {
               // System.out.println("aTime.toString() = " + aTime.toString());
                semTime2 = aTime;
            }
        }
        if (semTime1!=null && semTime2!=null) {
            if (semTime1.getOwlTime().getDateString().equals(semTime2.getOwlTime().getDateString())){
                return true;
            }
        }
        return false;
    }


    //// casting functions
    public static ArrayList<SemTime> castToTime (ArrayList<SemObject> semObjects) {
        ArrayList<SemTime> mySemTimes = new ArrayList<SemTime>();
        for (int i = 0; i < semObjects.size(); i++) {
            SemTime semTime = (SemTime) semObjects.get(i);
            mySemTimes.add(semTime);
        }
        return mySemTimes;
    }

    public static ArrayList<SemEvent> castToEvent (ArrayList<SemObject> semObjects) {
        ArrayList<SemEvent> mySemEvents = new ArrayList<SemEvent>();
        for (int i = 0; i < semObjects.size(); i++) {
            SemEvent event = (SemEvent) semObjects.get(i);
            mySemEvents.add(event);
        }
        return mySemEvents;
    }

    public static ArrayList<SemActor> castToActor (ArrayList<SemObject> semObjects) {
        ArrayList<SemActor> mySemActors = new ArrayList<SemActor>();
        for (int i = 0; i < semObjects.size(); i++) {
            SemActor actor = (SemActor) semObjects.get(i);
            mySemActors.add(actor);
        }
        return mySemActors;
    }

    public static ArrayList<SemPlace> castToPlace (ArrayList<SemObject> semObjects) {
        ArrayList<SemPlace> mySemPlaces = new ArrayList<SemPlace>();
        for (int i = 0; i < semObjects.size(); i++) {
            SemPlace place = (SemPlace) semObjects.get(i);
            mySemPlaces.add(place);
        }
        return mySemPlaces;
    }

    ///////////////////////////////

/*    static public String cleanDbpediaUri(String uri, String ns) {
        String cleanUri = ns;
        // <http://dbpedia.org/resource/MG_F_/_MG_TF>
        if (uri.startsWith(ns)) {
            for (int i = ns.length(); i < uri.toCharArray().length; i++) {
                char c = uri.toCharArray()[i];
                if ((c!='.') &&
                    (c!='&') &&
                    (c!='*') &&
                    (c!=':') &&
                    (c!='!') &&
                    (c!='!') &&
                    (c!='+') &&
                    (c!='-') &&
                    (c!='–') &&
                    (c!='–') &&
                    (c!='(') &&
                    (c!='/') &&
                    (c!='!') &&
                    (c!=',') &&
                    (c!='\'') &&
                    (c!=')')
                        ) {
                   cleanUri+=c;
                }
                else {
                     if (c=='-') { cleanUri += "_"; }
                     if (c=='–') { cleanUri += "_"; }
                }
            }
            cleanUri = cleanUri.replaceAll("%23","");
            cleanUri = cleanUri.replaceAll("%3F","");
            cleanUri = cleanUri.replaceAll("%7C","");
            cleanUri = cleanUri.replaceAll("%22","");
        }
        else {
            System.out.println("uri = " + uri);
        }
        return cleanUri;
    }

    static public String cleanUri(String uri) {
        String cleanUri = "";
        for (int i = 0; i < uri.toCharArray().length; i++) {
            char c = uri.toCharArray()[i];
            if ((c!='.') &&
                (c!='&') &&
                (c!='*') &&
                (c!=':') &&
                (c!='!') &&
                (c!='!') &&
                (c!='+') &&
                (c!='-') &&
                (c!='–') &&
                (c!='–') &&
                (c!='(') &&
                (c!='/') &&
                (c!='!') &&
                (c!=',') &&
                (c!='\'') &&
                (c!=')')
                    ) {
               cleanUri+=c;
            }
            else {
                 if (c=='-') { cleanUri += "_"; }
                 if (c=='–') { cleanUri += "_"; }
            }
        }
        cleanUri = cleanUri.replaceAll("%23","");
        cleanUri = cleanUri.replaceAll("%3F","");
        cleanUri = cleanUri.replaceAll("%7C","");
        cleanUri = cleanUri.replaceAll("%22","");

        return cleanUri.replace(" ", "");
    }*/

    static public String alphaNumericUri(String uri) {
        final String alfanum="1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String cleanUri = "";
        for (int i = 0; i < uri.toCharArray().length; i++) {
            char c = uri.toCharArray()[i];
            if (alfanum.indexOf(c)>-1) {
               cleanUri +=c;
            }
        }
        return cleanUri;
    }

    static public boolean hasAlphaNumeric(String uri) {
/*
        final String alfanum="1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < uri.toCharArray().length; i++) {
            char c = uri.toCharArray()[i];
            if (alfanum.indexOf(c)>-1) {
               return true;
            }
        }
        return false;
*/      /// had to take out this function because of Bulgarian
        return true;
    }


    static public ArrayList<File> makeRecursiveFileList(File inputFile) {
        ArrayList<File> acceptedFileList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    ArrayList<File> nextFileList = makeRecursiveFileList(newFile);
                    acceptedFileList.addAll(nextFileList);
                } else {
                    acceptedFileList.add(newFile);
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File does not exist!");
            }
        }
        return acceptedFileList;
    }

    static public ArrayList<File> makeRecursiveFileList(File inputFile, String theFilter) {
        ArrayList<File> acceptedFileList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead())) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    ArrayList<File> nextFileList = makeRecursiveFileList(newFile, theFilter);
                    acceptedFileList.addAll(nextFileList);
                } else {
                    if (newFile.getName().endsWith(theFilter)) {
                        acceptedFileList.add(newFile);
                    }
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File/folder does not exist!");
            }
        }
        return acceptedFileList;
    }

    static public ArrayList<File> makeFlatFileList(File inputFile, String theFilter) {
        ArrayList<File> acceptedFileList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (!newFile.isDirectory()) {
                    if (newFile.getName().endsWith(theFilter)) {
                        acceptedFileList.add(newFile);
                    }
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File does not exist!");
            }
        }
        return acceptedFileList;
    }

    static public ArrayList<File> makeFolderList(File inputFile) {
        ArrayList<File> folderList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    folderList.add(newFile);
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File does not exist!");
            }
        }
        return folderList;
    }




    static public HashMap<String, ArrayList<String>> ReadFileToStringHashMap(String fileName) {
        HashMap<String, ArrayList<String>> lineHashMap = new HashMap<String, ArrayList<String>>();
        if (new File(fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        int idx_s = inputLine.indexOf("\t");
                        if (idx_s>-1) {
                            String key = inputLine.substring(0, idx_s).trim();
                            String value = inputLine.substring(idx_s+1).trim();
                            if (lineHashMap.containsKey(key)) {
                                ArrayList<String> files = lineHashMap.get(key);
                                files.add(value);
                                lineHashMap.put(key, files);
                            }
                            else {
                                ArrayList<String> files = new ArrayList<String>();
                                files.add(value);
                                lineHashMap.put(key, files);
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lineHashMap;
    }

    static public Vector<String> ReadFileToStringVector(String fileName) {
        Vector<String> vector = new Vector<String>();
        if (new File(fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        vector.add(inputLine.trim().toLowerCase());
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return vector;
    }


}
