package eu.newsreader.eventcoreference.objects;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/9/12
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class CorefTarget {
    
    private String termId;
    private String docId;
    private String sentenceId;
    private String synset;
    private double synsetScore;
    private String word;
    private double corefScore;
    private double simScore;
    private double granScore;
    private double domScore;
    private String domain;
    private String granularityNumber;
    private String granularityType;
    private String pos;

    public CorefTarget() {
        this.pos = "";
        this.domain = "";
        this.granularityNumber = "";
        this.granularityType = "";
        this.termId = "";
        this.docId = "";
        this.synset = "";
        this.sentenceId = "";
        this.synsetScore = 0;
        this.word = "";
        this.corefScore = 0;
        this.simScore = 0;
        this.granScore = 0;
        this.domScore = 0;
    }

    public CorefTarget(String termId) {
        this.termId = termId;
        this.pos = "";
        this.domain = "";
        this.granularityNumber = "";
        this.granularityType = "";
        this.docId = "";
        this.sentenceId = "";
        this.synset = "";
        this.synsetScore = 0;
        this.word = "";
        this.corefScore = 0;
        this.simScore = 0;
        this.granScore = 0;
        this.domScore = 0;
    }

    public String getGranularityType() {
        return granularityType;
    }

    public void setGranularityType(String granularityType) {
        this.granularityType = granularityType;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public double getDomScore() {
        return domScore;
    }

    public void setDomScore(double domScore) {
        this.domScore = domScore;
    }

    public double getGranScore() {
        return granScore;
    }

    public void setGranScore(double granScore) {
        this.granScore = granScore;
    }

    public double getSimScore() {
        return simScore;
    }

    public void setSimScore(double simScore) {
        this.simScore = simScore;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getGranularityNumber() {
        return granularityNumber;
    }

    public void setGranularityNumber(String granularityNumber) {
        this.granularityNumber = granularityNumber;
    }

    public String getSentenceId() {
        return sentenceId;
    }

    public void setSentenceId(String sentenceId) {
        this.sentenceId = sentenceId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public String getSynset() {
        return synset;
    }

    public void setSynset(String synset) {
        this.synset = synset;
    }

    public double getSynsetScore() {
        return synsetScore;
    }

    public void setSynsetScore(double synsetScore) {
        this.synsetScore = synsetScore;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getCorefScore() {
        return corefScore;
    }

    public void setCorefScore(double corefScore) {
        this.corefScore = corefScore;
    }

    String extractFileIdFromTermId () {
        String str = termId;
        int idx = str.indexOf(".kaf");
        if (idx>-1) {
            str = str.substring(0,idx)+".txt";
        }
        return str;
    }


    public String toString () {
        String str = "\t\t<target termId=\""+ termId+"\"";
        if (!docId.isEmpty()) str +=" docId=\""+docId+"\"";
        if (!sentenceId.isEmpty()) str +=" sentenceId=\""+sentenceId+"\"";
        if (simScore>0) str +=" simScore=\""+simScore+"\"";
        if (granScore>0) str +=" granScore=\""+granScore+"\"";
        if (domScore>0) str +=" domScore=\""+domScore+"\"";
        if (!domain.isEmpty()) str +=" domain=\""+domain+"\"";
        if (!granularityNumber.isEmpty()) str +=" granularityNumber=\""+ granularityNumber+"\"";
        if (!granularityType.isEmpty()) str +=" granularityType=\""+ granularityType+"\"";
        if (!pos.isEmpty()) str +=" pos=\""+pos+"\"";
        if (corefScore>0) str +=" corefScore=\""+corefScore+"\"";
        if (!synset.isEmpty()) str +=" synset=\""+synset+"\"";
        if (synsetScore>0) str +=" rank=\""+synsetScore+"\"";
        if (!word.isEmpty()) str +=" word=\""+word+"\"";
        str += "/>\n";
        return str;
    }



    /*  THIS IS A HACK TO CLEAN UP THE IDENTIFIERS FOR ECB. NEVER USE THIS AGAIN
    		<event>
			<target termId="eecb1.0:44/3.eecb.kaf.step.0.step.1.step.2.ont.kaf.ont.kaf.offset.kaf_stanford-parser-en#t133" docId="TOPIC_44_EVENT_COREFERENCE_CORPUS" sentenceId="s5" simScore="0.0" granScore="0.0" domScore="0.0" domain="" granularityNumber="" granularityType="" pos="" corefScore="1.0" synset="eng-30-00079018-n" rank="0.0" word="purchase"/>
		</event>
		<participants>
			<participant id="39" lcs="service" score="1.0" synset="eng-30-08198137-n" label="service" mentions="27">
					<target termId="eecb1.0:44/4.eecb.kaf.step.0.step.1.step.2.ont.kaf.ont.kaf.offset.kafstanford-parser-en#t111" docId="TOPIC_44_EVENT_COREFERENCE_CORPUS" sentenceId="s5" simScore="0.0" granScore="0.0" domScore="0.0" domain="" granularityNumber="" granularityType="" pos="" corefScore="1.0" synset="eng-30-08198137-n" rank="0.0168523" word="service"/>
					<target termId="eecb1.0:44/4.eecb.kaf.step.0.step.1.step.2.ont.kaf.ont.kaf.offset.kafstanford-parser-en#t111" docId="TOPIC_44_EVENT_COREFERENCE_CORPUS" sentenceId="s5" simScore="0.0" granScore="0.0" domScore="0.0" domain="" granularityNumber="" granularityType="" pos="" corefScore="1.0" synset="eng-30-08198137-n" rank="0.0168523" word="service"/>
			</participant>

     */
 /*   public String toString () {
        String str = "\t\t<target termId=\""+ termId +"\" docId=\""+extractFileIdFromTermId()+"\" topicId=\""+docId+"\" sentenceId=\""+sentenceId+
                "\" simScore=\""+simScore+"\" granScore=\""+granScore+"\" domScore=\""+domScore+
                "\" domain=\""+domain+"\" granularityNumber=\""+ granularityNumber +"\" granularityType=\""+ granularityType+"\" pos=\""+pos+
                "\" corefScore=\""+corefScore+"\" synset=\""+synset+"\" rank=\""+synsetScore+"\" word=\""+word+"\"/>\n";
        return str;
    }*/

    public String toOpenElementString () {
        String str = "\t\t<target termId=\""+ termId +"\" docId=\""+docId+"\" sentenceId=\""+
                "\" simScore=\""+simScore+"\" granScore=\""+granScore+"\" domScore=\""+domScore+
                "\" domain=\""+domain+"\" granularityNumber=\""+ granularityNumber +"\" granularityType=\""+ granularityType+"\" pos=\""+pos+
                sentenceId+"\" corefScore=\""+corefScore+"\" synset=\""+synset+"\" rank=\""+synsetScore+"\" word=\""+word+"\">\n";
        return str;
    }
}
