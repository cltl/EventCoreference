package eu.newsreader.eventcoreference.objects;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventMention {
  /*
  <semEvent id="e30" lcs="raid" score="2.4849066497880004" synset="eng-30-02020027-v" label="raid" mentions="2">
	<mentions>
	<event-mention>
		<event>
			<target termId="t285" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="eng-30-02020027-v" rank="0.257681" word="raid"/>
		<event>
		<participants>
			<participant id="p30" lcs="eng-30-00007846-n" score="2.639057329615259" synset="eng-30-10210137-n" label="rebel" mentions="26">
					<target termId="t288" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-11346710-n" rank="0.227748" word="town"/>
			</participant>
			<participant id="p93" lcs="" score="0.0" synset="" label="Khalanga" mentions="1">
					<target termId="t2810" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="" rank="0.0" word="Khalanga"/>
			</participant>
			<participant id="p34" lcs="eng-30-08008335-n" score="2.639057329615259" synset="eng-30-08209687-n" label="police" mentions="16">
					<target termId="t2827" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-08337324-n" rank="0.143377" word="office"/>
					<target termId="t2830" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-08051946-n" rank="0.0895559" word="court"/>
			</participant>
		</participants>
		<times>
			<time id="e3" lcs="eng-30-15163157-n" score="2.890371757896165" synset="eng-30-15163979-n" label="Monday" mentions="9">
					<target termId="t284" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="eng-30-15164570-n" rank="1.0" word="Saturday"/>
			</time>
		</times>
		<locations>
		</locations>
	</event-mention>

   */

   private double pScore;
   private double tScore;
   private double lScore;
    
   private int nP;
   private int nT;
   private int nL;
   
   private double score;
   private CorefTarget event;
   private CoRefSet participants;
   private CoRefSet times;
   private CoRefSet locations;

    public EventMention() {
        this.nL = 0;
        this.nP = 0;
        this.nT = 0;
        this.pScore = 0;
        this.tScore = 0;
        this.lScore = 0;
        this.score = 0;
        this.event = new CorefTarget();;
        this.locations = new CoRefSet();;
        this.participants = new CoRefSet();;
        this.times = new CoRefSet();
    }


    public void scoreEventMention (String method) {
        if (method.equalsIgnoreCase("component-factor")) {
            scoreEventMentionComponentFactor();
        }
        else if (method.equalsIgnoreCase("component-average")) {
            scoreEventMentionComponentAverage();
        }
        else score = this.event.getCorefScore();
    }

    public void scoreEventMentionComponentFactor () {
        score = this.event.getCorefScore();
        if (nP>0) {
           score *= pScore; 
        }
        if (nT>0) {
            score *= tScore;
        }
        if (nL>0) {
            score *= lScore;
        }
    }

    public void scoreEventMentionComponentAverage () {
        score = this.event.getCorefScore();
        double add = pScore+tScore+lScore;
       // score  += (add/3);
        score  += add;
    }

    public double getScore() {
        return score;
    }

    public int getIntScore() {
        int intScore = (int) (100*score);
        return intScore;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getnL() {
        return nL;
    }

    public void setnL(int nL) {
        this.nL = nL;
    }

    public int getnP() {
        return nP;
    }

    public void setnP(int nP) {
        this.nP = nP;
    }

    public int getnT() {
        return nT;
    }

    public void setnT(int nT) {
        this.nT = nT;
    }

    public double getlScore() {
        return lScore;
    }

    public void setlScore(double lScore) {
        this.lScore = lScore;
    }

    public double getpScore() {
        return pScore;
    }

    public void setpScore(double pScore) {
        this.pScore = pScore;
    }

    public double gettScore() {
        return tScore;
    }

    public void settScore(double tScore) {
        this.tScore = tScore;
    }

    public CorefTarget getEvent() {
        return event;
    }

    public void setEvent(CorefTarget event) {
        this.event = event;
    }

    public CoRefSet getLocations() {
        return locations;
    }

    public void setLocations(CoRefSet locations) {
        this.locations = locations;
    }

    public void addLocationTarget(CorefTarget location) {
        this.locations.addTarget(location);
    }

    public CoRefSet getParticipants() {
        return participants;
    }

    public void setParticipants(CoRefSet participants) {
        this.participants = participants;
    }

    public void addParticipantTarget(CorefTarget participant) {
        this.participants.addTarget(participant);
    }

    public CoRefSet getTimes() {
        return times;
    }

    public void setTimes(CoRefSet times) {
        this.times = times;
    }
    
    public void addTimeTarget(CorefTarget time) {
        this.times.addTarget(time);
    }
    
    public String toString () {
        String str = "";
//        str += "<event-mention>\n";
        str += "\t\t<event score=\""+score+"\" pScore=\""+pScore+"\" tScore=\""+tScore+"\" lScore=\""+lScore+"\"";
        str += " nP=\""+nP+"\" nL=\""+nL+"\" nT=\""+nT+"\"";
        str += ">\n"+event.toString()+"\t\t</event>\n";
/*
        str += "<participants>\n";
        for (int i = 0; i < participants.getTargets().size(); i++) {
            CorefTarget p =  participants.getTargets().get(i);
            str += p.toString();
            
        }
        str += "</participants>\n";
        str += "<times>\n";
        for (int i = 0; i < times.getTargets().size(); i++) {
            CorefTarget corefTarget = times.getTargets().get(i);
            str += corefTarget.toString();
        }
        str += "</times>\n";
        str += "<locations>\n";
        for (int i = 0; i < locations.getTargets().size(); i++) {
            CorefTarget corefTarget = locations.getTargets().get(i);
            str += corefTarget.toString();
        }
        str += "</locations>\n";
        str += "</event-mention>\n";
*/

        return str;
    }


    /*
   <semEvent id="e30" lcs="raid" score="2.4849066497880004" synset="eng-30-02020027-v" label="raid" mentions="2">
     <mentions>
     <event-mention>
         <event>
             <target termId="t285" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="eng-30-02020027-v" rank="0.257681" word="raid"/>
         <event>
         <participants>
             <participant id="p30" lcs="eng-30-00007846-n" score="2.639057329615259" synset="eng-30-10210137-n" label="rebel" mentions="26">
                     <target termId="t288" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-11346710-n" rank="0.227748" word="town"/>
             </participant>
             <participant id="p93" lcs="" score="0.0" synset="" label="Khalanga" mentions="1">
                     <target termId="t2810" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="" rank="0.0" word="Khalanga"/>
             </participant>
             <participant id="p34" lcs="eng-30-08008335-n" score="2.639057329615259" synset="eng-30-08209687-n" label="police" mentions="16">
                     <target termId="t2827" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-08337324-n" rank="0.143377" word="office"/>
                     <target termId="t2830" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-08051946-n" rank="0.0895559" word="court"/>
             </participant>
         </participants>
         <times>
             <time id="e3" lcs="eng-30-15163157-n" score="2.890371757896165" synset="eng-30-15163979-n" label="Monday" mentions="9">
                     <target termId="t284" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="eng-30-15164570-n" rank="1.0" word="Saturday"/>
             </time>
         </times>
         <locations>
         </locations>
     </event-mention>
 
    */
}
