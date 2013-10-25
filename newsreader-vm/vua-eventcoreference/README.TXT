Project: VUA-EventCoreference
Version: 0.1
Date: October 2013
Copyright: VU University Amsterdam, Piek Vossen
Email: piek.vossen@vu.nl

FILE LISTING:
	COPYING-GPL.TXT
	LICENSESOFTWARE.TXT
	README.TXT
	INSTALL
	run.sh
	example:
		enPM.coref.naf
		enPM.naf
	lib:
		EventCoreference-1.0-SNAPSHOT.jar
		KyotoKafSaxParser-1.0-jar-with-dependencies.jar
	scripts:
		event-coref-naf-file.sh

LICENSE:
This work is licensed under a GNU GPL version 3.0: <http://fsf.org/>. See the file LICENSESOFTWARE.TXT and COPYING-GPL.TXT that should be in the
top-directory of this distribution.

DESCIPTION:

VUA-EventCoreference reads an input stream in the Natural Language Processing Annotation Format (NAF) and produces an output stream in NAF with a coreference
layer for events. The program assumes the following layers to be present, depending on the function used:


eu.newsreader.eventcoreference.naf.EventCorefLemmaBaseline
<text>			token layer
<terms>			term layer
<predicates>	predicate and SRL layer

OUTPUT

    <coref id="coe19" type="event">
      <span>
        <target id="t118">
          <!--raise-->
        </target>
      </span>
    </coref>
    <coref id="coe24" type="event">
      <span>
        <target id="t131">
          <!--arsenal-->
        </target>
      </span>
    </coref>
    <coref id="coe10" type="event">
      <span>
        <target id="t55">
          <!--insist-->
        </target>
      </span>
    </coref>

VUA-EventCoreference is compiled on Mac OS X version 10.6.8 with Java 1.6.

