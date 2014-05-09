package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.newsreader.eventcoreference.naf.ResourcesUri;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemTime extends SemObject{
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

    /*
    2. time aspects

I see that you define the time instances with a prefix “tl:”,
referring to the timeline ontology.
Are you suggesting to use that ontology to model time,
or are you using the prefix to define the URI of the time instance?
In the former case, additional assertions have to be added to define the object
(e.g. type - tl:Instant, tl:Interval - , tl:start, etc),
otherwise we just end up with an instance with a label attached to it,
which cannot be actually exploited.
In the latter case, we don’t need to introduce this namespace,
we can directly use the nwr one.

Note that the "additional assertions" comment applies also
if we adopt the owl:time ontology (our suggestion - see nwr:20010101 in the example.trig in attachment).

	#acquisition event between two companies (actor_01, actor_02), at a certain date/time
    nwr:event_01
        a rsem:Event, newsEvents:Acquisition ;
        rsem:hasActor nwr:actor_01, nwr:actor_02 ;
        rsem:hasTime nwr:time_01 .

    nwr:actor_01
        a rsem:Actor, dbpedia:Company .

    nwr:actor_02
        a rsem:Actor, dbpedia:Company .

    nwr:time_01
        a rsem:Time, owltime:Instant ;
        owltime:inDateTime nwr:20010101 .

    nwr:20010101
        owltime:day "1"^^xsd:int ;
        owltime:month "1"^^xsd:int ;
        owltime:year "2001"^^xsd:int .
     */

   private OwlTime owlTime;

   public SemTime() {
       owlTime = new OwlTime();
    }



    public String toString () {
        String str = "<semTime id=\""+this.getId()+"\" lcs=\""+this.getLabel()+"\" score=\""+this.getScore()+"\" label=\""+this.getLabel()+"\" mentions=\""+this.getNafMentions().size()+"\">\n";
        str += "<mentions>\n";
        for (int i = 0; i < this.getNafMentions().size(); i++) {
            NafMention s = this.getNafMentions().get(i);
            str += s.toString()+"\n";
        }
/*
        for (int i = 0; i < this.getMentions().size(); i++) {
            ArrayList<eu.kyotoproject.kaf.CorefTarget> mentions = this.getMentions().get(i);
            for (int j = 0; j < mentions.size(); j++) {
                eu.kyotoproject.kaf.CorefTarget corefTarget = mentions.get(j);
                str += corefTarget.toString();
            }
        }
*/
        str += "</mentions>\n";
        str += "</semEvent>\n";
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

    public OwlTime getOwlTime() {
        return owlTime;
    }

    public void setOwlTime(OwlTime owlTime) {
        this.owlTime = owlTime;
    }

    public void addToJenaModelTimeInstant(Model model, OwlTime owlTime) {
        this.getOwlTime().addToJenaModelOwlTimeInstant(model);

        Resource resource = model.createResource(this.getURI());
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
        }

        resource.addProperty(RDF.type, Sem.Time);

        Resource aResource = model.createResource(ResourcesUri.owltime + "Instant");
        resource.addProperty(RDF.type, aResource);

        Resource value = model.createResource(owlTime.getDateString());
        Property property = model.createProperty(ResourcesUri.owltime + "inDateTime");
        resource.addProperty(property, value);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property gaf = model.createProperty(ResourcesUri.gaf + "denotedBy");
            Resource targetResource = model.createResource(nafMention.toString());
            resource.addProperty(gaf, targetResource);

        }
    }

    public void addToJenaModelDocTimeInstant(Model model) {
        if (nafMentions.size() > 0) {
            OwlTime owlTime = new OwlTime();
            owlTime.parsePublicationDate(phraseCounts.get(0).getPhrase());
            owlTime.addToJenaModelOwlTimeInstant(model);

            Resource resource = model.createResource(this.getURI());
            for (int i = 0; i < phraseCounts.size(); i++) {
                PhraseCount phraseCount = phraseCounts.get(i);
                resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
            }

            resource.addProperty(RDF.type, Sem.Time);

            Resource aResource = model.createResource(ResourcesUri.owltime + "Instant");
            resource.addProperty(RDF.type, aResource);
            Resource value = model.createResource(owlTime.getDateString());
            Property property = model.createProperty(ResourcesUri.owltime + "inDateTime");
            resource.addProperty(property, value);


        }
    }

    public void addToJenaModelTimeInterval(Model model) {
        this.getOwlTime().addToJenaModelOwlTimeDuration(model);

        Resource resource = model.createResource(this.getURI());
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
        }

        resource.addProperty(RDF.type, Sem.Time);

        Resource interval = model.createResource(ResourcesUri.owltime + "Interval");
        resource.addProperty(RDF.type, interval);

        Resource value = model.createResource(owlTime.getDateString());
        Property property = model.createProperty(ResourcesUri.owltime + "inDateTime");
        resource.addProperty(property, value);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property gaf = model.createProperty(ResourcesUri.gaf + "denotedBy");
            Resource targetResource = model.createResource(nafMention.toString());
            resource.addProperty(gaf, targetResource);

        }

    }

    public void addToJenaModelDocTimeInterval(Model model) {
        if (phraseCounts.size() > 0) {
            OwlTime owlTime = new OwlTime();
            owlTime.parsePublicationDate(getPhrase());
            owlTime.addToJenaModelOwlTimeInstant(model);

            Resource resource = model.createResource(this.getURI());
            for (int i = 0; i < phraseCounts.size(); i++) {
                PhraseCount phraseCount = phraseCounts.get(i);
                resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
            }

            resource.addProperty(RDF.type, Sem.Time);
            Resource interval = model.createResource(ResourcesUri.owltime + "Interval");
            resource.addProperty(RDF.type, interval);

            Resource value = model.createResource(owlTime.getDateString());
            Property property = model.createProperty(ResourcesUri.owltime + "inDateTime");
            resource.addProperty(property, value);
        }
    }
}
