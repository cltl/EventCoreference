package eu.newsreader.eventcoreference.objects;

import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemObject {
  /*
  <semEvent id="e30" lcs="raid" score="2.4849066497880004" concept="eng-30-02020027-v" label="raid" mentions="2">
	<mentions>
	<event-mention>
		<event>
			<target termId="t285" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" concept="eng-30-02020027-v" rank="0.257681" word="raid"/>
		<event>
		<participants>
			<participant id="p30" lcs="eng-30-00007846-n" score="2.639057329615259" concept="eng-30-10210137-n" label="rebel" mentions="26">
					<target termId="t288" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" concept="eng-30-11346710-n" rank="0.227748" word="town"/>
			</participant>
			<participant id="p93" lcs="" score="0.0" concept="" label="Khalanga" mentions="1">
					<target termId="t2810" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" concept="" rank="0.0" word="Khalanga"/>
			</participant>
			<participant id="p34" lcs="eng-30-08008335-n" score="2.639057329615259" concept="eng-30-08209687-n" label="police" mentions="16">
					<target termId="t2827" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" concept="eng-30-08337324-n" rank="0.143377" word="office"/>
					<target termId="t2830" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" concept="eng-30-08051946-n" rank="0.0895559" word="court"/>
			</participant>
		</participants>
		<times>
			<time id="e3" lcs="eng-30-15163157-n" score="2.890371757896165" concept="eng-30-15163979-n" label="Monday" mentions="9">
					<target termId="t284" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" concept="eng-30-15164570-n" rank="1.0" word="Saturday"/>
			</time>
		</times>
		<locations>
		</locations>
	</event-mention>

   */

   private String id;
   private double score;
   private ArrayList<KafSense> concepts;
   private ArrayList<PhraseCount> phraseCounts;
   private String lcs;
   private String label;
   private ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>> mentions;

   public SemObject() {
        this.mentions = new ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>>();;
        this.id = "";
        this.label = "";
        this.lcs = "";
        this.score = 0;
        this.concepts = new ArrayList<KafSense>();
        this.phraseCounts = new ArrayList<PhraseCount>();
    }

    public void setConcepts(ArrayList<KafSense> concepts) {
        this.concepts = concepts;
    }

    public ArrayList<PhraseCount> getPhraseCounts() {
        return phraseCounts;
    }

    public void setPhraseCounts(ArrayList<PhraseCount> phraseCounts) {
        this.phraseCounts = phraseCounts;
    }

    public void addPhraseCounts(String phrase) {
        boolean match = false;
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount count = phraseCounts.get(i);
            if (count.getPhrase().equals(phrase)) {
                count.incrementCount();
                match = true;
                break;
            }
        }
        if (!match) {
            phraseCounts.add(new PhraseCount(phrase, 1));
        }
    }

    public void addPhraseCountsForMentions (KafSaxParser kafSaxParser) {
        for (int i = 0; i < mentions.size(); i++) {
            ArrayList<CorefTarget> corefTarget = mentions.get(i);
            String phrase = "";
            for (int j = 0; j < corefTarget.size(); j++) {
                CorefTarget target = corefTarget.get(j);
                KafTerm kafTerm = kafSaxParser.getTerm(target.getId());
                if (kafTerm!=null) {
                    phrase += " "+kafTerm.getLemma();
                }
            }
            if (!phrase.isEmpty()) {
                addPhraseCounts(phrase);
            }
        }
    }
    public ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>> getMentions() {
        return mentions;
    }

    public void setMentions(ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>> mentions) {
        this.mentions = mentions;
    }

    public void addMentions(ArrayList<eu.kyotoproject.kaf.CorefTarget> mentions) {
        this.mentions.add(mentions);
    }

    public void addMention(ArrayList<eu.kyotoproject.kaf.CorefTarget> mention) {
        this.mentions.add(mention);
    }

    public String getLcs() {
        return lcs;
    }

    public void setLcs(String lcs) {
        this.lcs = lcs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public ArrayList<KafSense> getConcepts() {
        return concepts;
    }

    public void setConcept(ArrayList<KafSense> concepts) {
        this.concepts = concepts;
    }

    public void addConcepts(ArrayList<KafSense> concepts) {
        this.concepts.addAll(concepts);
    }

    public void addConcept(KafSense concept) {
        this.concepts.add(concept);
    }


}
