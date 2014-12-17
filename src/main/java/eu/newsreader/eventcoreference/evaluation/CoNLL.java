package eu.newsreader.eventcoreference.evaluation;

import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafCoreferenceSet;
import eu.kyotoproject.kaf.KafWordForm;

import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by piek on 12/13/14.
 */
public class CoNLL {


    /**
     * #begin document (LuoTestCase);
     test1	0	0	a1	(0
     test1	0	1	a2	0)
     test1	0	2	junk	-
     test1	0	3	b1	(1
     test1	0	4	b2	-
     test1	0	5	b3	-
     test1	0	6	b4	1)
     test1	0	7	jnk	-
     test1	0	8	.	-

     test2	0	0	c	(1)
     test2	0	1	jnk	-
     test2	0	2	d1	(2
     test2	0	3	d2	2)
     test2	0	4	jnk	-
     test2	0	5	e	(2)
     test2	0	6	jnk	-
     test2	0	7	f1	(2
     test2	0	8	f2	-
     test2	0	9	f3	2)
     test2	0	10	.	-
     #end document
     */

    /**
     *
     * @param stream
     * @param fileName
     * @param type
     */
    static public void serializeToCoNLL (OutputStream stream,  String fileName, String type,
                                         ArrayList<KafWordForm> kafWordFormArrayList,
                                         ArrayList<KafCoreferenceSet> kafCoreferenceSetArrayList) {
        try {
            String str = "#begin document ("+fileName+");";
            stream.write(str.getBytes());
            str  = "";
            boolean COREFERRING = false;
            boolean NEWSENTENCE = false;
            String currentSentence = "";
            String currentReference = "";

            for (int i = 0; i < kafWordFormArrayList.size(); i++) {
                KafWordForm kafWordForm = kafWordFormArrayList.get(i);
                /// insert sentence separator
                if (!currentSentence.isEmpty() && !currentSentence.equals(kafWordForm.getSent()))  {
                    NEWSENTENCE = true;
                    currentSentence = kafWordForm.getSent();
                }
                else if (currentSentence.isEmpty()) {
                    /// first sentence
                    currentSentence = kafWordForm.getSent();
                }
                else {
                    NEWSENTENCE = false;
                }
                String corefId = getCoreferenceSetId(kafCoreferenceSetArrayList,kafWordForm.getWid(), type);
                //System.out.println(kafWordForm.getWid()+":" + corefId);
                //// First we need to handle the previous line if any
                //// After that we can process the current
                /// check previous conditions and terminate properly

                if (corefId.isEmpty()) {
                    //// current is not a coreferring token
                    if (COREFERRING) {
                        //// previous was coreferring so we need to terminate the previous with ")"
                        str += ")";
                    }
                    /// always terminate the previous token
                    str += "\n";
                    COREFERRING = false;
                    /// we started a new sentence so we insert a blank line
                    if (NEWSENTENCE) str+= "\n";
                    /// add the info for the current token
                    String tokenId = kafWordForm.getWid();
                    if (tokenId.startsWith("w")) {
                        tokenId = tokenId.substring(1);
                    }
                    str += fileName+"\t"+kafWordForm.getSent()+"\t"+tokenId+"\t"+kafWordForm.getWf() +"\t"+"-";
                }
                else {
                    if (NEWSENTENCE) {
                        /// we started a new sentence so we insert a blank line
                        if (COREFERRING) {
                            /// end of sentence implies ending coreference as well
                            str += ")\n";
                        }
                        else {
                            str += "\n";
                        }
                        str+= "\n";
                    }
                    else {
                        if (COREFERRING && !currentReference.equals(corefId))  {
                            str += ")\n";
                        }
                        else {
                            str += "\n";
                        }
                    }
                    /// add the info for the current token
                    String tokenId = kafWordForm.getWid();
                    if (tokenId.startsWith("w")) {
                        tokenId = tokenId.substring(1);
                    }
                    str += fileName+"\t"+kafWordForm.getSent()+"\t"+tokenId+"\t"+kafWordForm.getWf() +"\t";
                    if (!COREFERRING) {
                        str += "("+corefId;
                        COREFERRING = true;
                    }
                    else {
                        if (!currentReference.equals(corefId)) {
                            str += "("+corefId;
                        }
                        else {
                            str += corefId;
                        }
                    }
                    currentReference = corefId;
                }
            }
            ///check the status of the last token
            if (COREFERRING) {
                str += ")\n";
            }
            else {
                str += "\n";
            }
            stream.write(str.getBytes());
            str = "#end document\n";
            stream.write(str.getBytes());

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static public String getCoreferenceSetId (ArrayList<KafCoreferenceSet> kafCoreferenceSetArrayList, String tokenId, String type) {
        String corefId = "";
        for (int i = 0; i < kafCoreferenceSetArrayList.size(); i++) {
            KafCoreferenceSet corefSet  = kafCoreferenceSetArrayList.get(i);
            // System.out.println("coref.getType() = " + corefSet.getType());
            if (type.isEmpty() || corefSet.getType().toLowerCase().startsWith(type.toLowerCase())) {
                for (int j = 0; j < corefSet.getSetsOfSpans().size(); j++) {
                    ArrayList<CorefTarget> corefTargets = corefSet.getSetsOfSpans().get(j);
                    for (int k = 0; k < corefTargets.size(); k++) {
                        CorefTarget corefTarget = corefTargets.get(k);
                        if (corefTarget.getId().equals(tokenId)) {
                            corefId = corefSet.getCoid();
                            //System.out.println("corefTarget.getId() = " + corefTarget.getId());
                            //System.out.println("tokenId = " + tokenId);
                            break;
                        }
                    }
                    if (!corefId.isEmpty()) {
                        break;
                    }
                }
            }
            else {
                //  if (!corefSet.getType().isEmpty()) System.out.println("coref.getType() = " + corefSet.getType());
            }
            if (!corefId.isEmpty()) {
                break;
            }
        }
       // if (!corefId.isEmpty()) System.out.println("corefId = " + corefId);
        return corefId;
    }
}
