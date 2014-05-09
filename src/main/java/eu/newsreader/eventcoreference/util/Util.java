package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.NafMention;
import eu.newsreader.eventcoreference.objects.SemObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {
    static public final int SPANLIMIT = 4;
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

    static public boolean hasObject(ArrayList<SemObject> objects, SemObject object) {
        for (int i = 0; i < objects.size(); i++) {
            SemObject semObject = objects.get(i);
            for (int j = 0; j < object.getNafMentions().size(); j++) {
                NafMention nafMention = object.getNafMentions().get(j);
                if (semObject.hasMention(nafMention)) {
                    return true;
                }

            }
        }
        return false;
    }

    static public ArrayList<KafSense> getExternalReferences(ArrayList<KafEntity> entities) {
        ArrayList<KafSense> refs = new ArrayList<KafSense>();
        for (int i = 0; i < entities.size(); i++) {
            KafEntity kafEntity = entities.get(i);
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
    static public HashMap<String, ArrayList<ArrayList<CorefTarget>>> getTimeMentionsHashMapFromSrl (KafSaxParser kafSaxParser) {
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentions = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (kafParticipant.getRole().equalsIgnoreCase("AM-TMP")) {
                    kafParticipant.setTokenStrings(kafSaxParser);
                    String uri = Util.cleanUri(kafParticipant.getTokenString());
                    if (mentions.containsKey(uri)) {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = mentions.get(uri);
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(uri, srlTargets);
                    }
                    else {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(uri,srlTargets);
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
                if (kafParticipant.getRole().equalsIgnoreCase("AM-LOC")) {
                    //String srl = kafParticipant.getId();
                    kafParticipant.setTokenStrings(kafSaxParser);
                    String uri = Util.cleanUri(kafParticipant.getTokenString());
                    if (mentions.containsKey(uri)) {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = mentions.get(uri);
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(uri, srlTargets);
                    }
                    else {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(uri,srlTargets);
                    }
                }
            }
        }
        return mentions;
    }

    static public HashMap<String, ArrayList<ArrayList<CorefTarget>>> getActorMentionsHashMapFromSrl (KafSaxParser kafSaxParser) {
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentions = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                //// SKIP LARGE PHRASES
                if (kafParticipant.getSpans().size()>SPANLIMIT) {
                    continue;
                }
                if ((kafParticipant.getRole().equalsIgnoreCase("a0")) ||
                    (kafParticipant.getRole().equalsIgnoreCase("a1")) ||
                    (kafParticipant.getRole().equalsIgnoreCase("a2")) ||
                    (kafParticipant.getRole().equalsIgnoreCase("a3"))){
                   // String srl = kafParticipant.getId();
                    kafParticipant.setTokenStrings(kafSaxParser);
                    String uri = Util.cleanUri(kafParticipant.getTokenString());
                    if (mentions.containsKey(uri)) {
                     //   System.out.println("srl = " + srl);
                        ArrayList<ArrayList<CorefTarget>> srlTargets = mentions.get(uri);
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(uri, srlTargets);
                    }
                    else {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(uri,srlTargets);
                    }
                }
            }
        }
        return mentions;
    }


/*    static public ArrayList<KafSense> getExternalReferencesEntities (KafSaxParser kafSaxParser, KafCoreferenceSet kafCoreferenceSet) {
        ArrayList<KafSense> references = new ArrayList<KafSense>();
        for (int i = 0; i < kafSaxParser.kafEntityArrayList.size(); i++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(i);
            boolean match = false;
            for (int j = 0; j < kafEntity.getSetsOfSpans().size(); j++) {
                ArrayList<CorefTarget> entityCorefTargets = kafEntity.getSetsOfSpans().get(j);
                for (int k = 0; k < entityCorefTargets.size(); k++) {
                    CorefTarget entityCorefTarget = entityCorefTargets.get(k);
                    for (int l = 0; l < kafCoreferenceSet.getSetsOfSpans().size(); l++) {
                        ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(l);
                        for (int m = 0; m < corefTargets.size(); m++) {
                            CorefTarget corefTarget = corefTargets.get(m);
                            if (entityCorefTarget.getId().equals(corefTarget.getId())) {
                                match = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (match) {
                references.addAll(kafEntity.getExternalReferences());
            }
        }
        return references;
    }*/


    /**
     * We assume entities can have a range of spans but such a range can be part of a coreference set. If so we extend the spans with the full coreference set
     * @param entitySpans
     * @param coreferenceSets
     * @return
     */
    static public ArrayList<ArrayList<CorefTarget>> getCoreferenceSetForEntitySpans (ArrayList<ArrayList<CorefTarget>> entitySpans,
                                                                                     ArrayList<KafCoreferenceSet> coreferenceSets) {
        ArrayList<ArrayList<CorefTarget>> corefSet = entitySpans;
        for (int i = 0; i < entitySpans.size(); i++) {
            ArrayList<CorefTarget> corefTargets = entitySpans.get(i);
            for (int j = 0; j < coreferenceSets.size(); j++){
                KafCoreferenceSet kafCoreferenceSet = coreferenceSets.get(j);
                if (matchingAtLeastOneSetOfSpans(corefTargets, kafCoreferenceSet.getSetsOfSpans())) {
                    for (int k = 0; k < kafCoreferenceSet.getSetsOfSpans().size(); k++) {
                        ArrayList<CorefTarget> targets = kafCoreferenceSet.getSetsOfSpans().get(k);
                        if (!hasCorefTargetArrayList(targets, corefSet)) {
                            corefSet.add(targets);
                        }
                    }
                }
            }
        }
        return corefSet;
    }


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

    static public boolean intersectingAtLeastOneSetOfSpans (ArrayList<ArrayList<CorefTarget>> spans1, ArrayList<ArrayList<CorefTarget>> spans2) {
        for (int i = 0; i < spans1.size(); i++) {
            ArrayList<CorefTarget> corefTargets1 = spans1.get(i);
            return matchingAtLeastOneSetOfSpans(corefTargets1, spans2);
        }
        return false;
    }

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

    static public boolean matchAllSpansOfAnObjectMentionOrTheRoleHead(KafSaxParser kafSaxParser, KafParticipant kafParticipant, SemObject semObject) {
        String headSpan = "";
        boolean functionWordPos = false;
        for (int i = 0; i < kafParticipant.getSpans().size(); i++) {
            CorefTarget corefTarget = kafParticipant.getSpans().get(i);
            if (!corefTarget.getHead().isEmpty()) {

               ///// A1, A2 and AM often have a preposition as the head which is mostly not part of the semObject mention
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
            /// all spans need to match for one of the mention sets
            for (int i = 0; i < semObject.getNafMentions().size(); i++) {
                ArrayList<NafMention> mentions = semObject.getNafMentions();
                boolean fullmatch = true;
                boolean submatch = false;
                for (int j = 0; j < mentions.size(); j++) {
                    NafMention nafMention = mentions.get(j);
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
/*                    if (semObject.getURI().equalsIgnoreCase("http://dbpedia.org/resource/Rio_de_Janeiro")) {
                        if (kafParticipant.getSpanIds().contains("t363")) {
                            System.out.println("submatch = " + submatch);
                            System.out.println("functionWordPos = " + functionWordPos);
                            System.out.println("kafParticipant = " + kafParticipant.getSpanIds());
                            System.out.println("semObject = " + semObject.getNafMentions());
                        }
                    }*/
                }
            }
            /// we did not find any full local match so we return false
            /// if we get here we know there is no match
            //// to soften this submatch is used but this is risky especially for longer spans of roles (can be complete sentences!)
            return false;
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
            ArrayList<ArrayList<CorefTarget>> sets = getCoreferenceSetForEntitySpans(corefTargetSets, kafSaxParser.kafCorefenceArrayList);
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
                    mention.addTokensId(kafWordForm.getWid());
                    if (!kafWordForm.getCharOffset().isEmpty()) {
                        int offSet = Integer.parseInt(kafWordForm.getCharOffset());
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
                    int offSet = Integer.parseInt(kafWordForm.getCharOffset());
                    int length = Integer.parseInt(kafWordForm.getCharLength());
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
        return mention;
    }

    /**
     *      Mention URI = News URI + "#char=START_OFFSET,END_OFFSET"
     */
/*    static public String getMentionUri (KafSaxParser kafSaxParser, String targetTerm) {
        String mentionTarget = targetTerm;
        int firstOffSet = -1;
        int highestOffSet = -1;
        int lengthOffSet = -1;
        KafTerm kafTerm = kafSaxParser.getTerm(targetTerm);
        for (int i = 0; i < kafTerm.getSpans().size(); i++) {
            String tokenId = kafTerm.getSpans().get(i);
            KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
            if (!kafWordForm.getCharOffset().isEmpty()) {
                int offSet = Integer.parseInt(kafWordForm.getCharOffset());
                int length = Integer.parseInt(kafWordForm.getCharLength());
                if (firstOffSet==-1 || firstOffSet>offSet) {
                    firstOffSet = offSet;
                }
                if (highestOffSet==-1 ||offSet>highestOffSet) {
                    highestOffSet = offSet;
                    lengthOffSet = length;
                }
            }
        }

        if (firstOffSet>-1 && highestOffSet>-1) {
            int end_offset = highestOffSet+lengthOffSet;
            mentionTarget += "#char="+firstOffSet+","+end_offset;
        }
        return mentionTarget;
    }*/

    static public String cleanDbpediaUri(String uri, String ns) {
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
    }

/*
    static public String getTermIdFromCorefTarget (CorefTarget corefTarget, String ID_SEPARATOR) {

       // http://www.newsreader-project.eu/2004_4_26_4C7M-RB90-01K9-42PW.xml#char=174,182&word=w30&term=t30
        /// ID-HACK
        String id = corefTarget.getId();
        int idx = id.lastIndexOf(ID_SEPARATOR);
        if (idx>-1) {
            id = id.substring(idx+1);
        }
//        /
        // char=174,182&word=w30&term=t30

        ///// ofset HACK
        idx = id.lastIndexOf("=");
        if (idx>-1) {
            id = id.substring(idx+1);
        }
        return id;
    }
*/

    static public String getTermIdFromMention (String mention, String ID_SEPARATOR) {

       // http://www.newsreader-project.eu/2004_4_26_4C7M-RB90-01K9-42PW.xml#char=174,182&word=w30&term=t30
        /// ID-HACK
        String id = mention;
        int idx = id.lastIndexOf(ID_SEPARATOR);
        if (idx>-1) {
            id = id.substring(idx+1);
        }
//        /
        // char=174,182&word=w30&term=t30

        ///// ofset HACK
        idx = id.lastIndexOf("=");
        if (idx>-1) {
            id = id.substring(idx+1);
        }
        return id;
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
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
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
                System.out.println("File does not exist!");
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

    static String getMentionReferenceFromCorefs(ArrayList<CorefTarget> corefs) {
        String mentionReference = "";
        String offset = "";
        String token = "";
        String term = "";
        for (int i = 0; i < corefs.size(); i++) {
            CorefTarget corefTarget = corefs.get(i);
            String [] fields = corefTarget.getId().split("&");
            if (fields.length==3) {
                String f1 = fields[0];
                String f2 = fields[1];
                String f3 = fields[2];

            }

        }
        return mentionReference;
    }

    static public HashMap<String, ArrayList<File>> makeFolderGroupList(File inputFile, int length, String filter) {
        HashMap<String, ArrayList<File>> folderList = new HashMap<String, ArrayList<File>>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    ArrayList<File> files = makeRecursiveFileList(newFile, filter);
                    String subs = newFile.getName();
                    if (subs.length()>length) {
                        subs = subs.substring(0, length);
                    }
                    if (folderList.containsKey(subs)) {
                        ArrayList<File> storedFiles = folderList.get(subs);
                        storedFiles.addAll(files);
                        folderList.put(subs, storedFiles);
                    }
                    else {
                        folderList.put(subs, files);
                    }
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


    static public HashMap ReadFileToStringHashMap(String fileName) {
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


    static public int copyFile(File inputFile, File outputFile) {
        if (!inputFile.exists()) {
            return -1;
        }
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
            byte[] buffer = new byte[(int) inputFile.length()];
            in.readFully(buffer);
            in.close();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));
            out.write(buffer);
        } catch (IOException e) {
            return -3;
        }
        return 0;
    }

}
