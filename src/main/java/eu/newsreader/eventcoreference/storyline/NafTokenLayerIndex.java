package eu.newsreader.eventcoreference.storyline;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafWordForm;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 20/07/16.
 */
public class NafTokenLayerIndex {

    public HashMap<String, ArrayList<KafWordForm>>  tokenMap;

    public NafTokenLayerIndex () {
        tokenMap = new HashMap<String, ArrayList<KafWordForm>>();
    }

    void createTokenIndex (File folder, String filter) {
        ArrayList<File> nafFiles = Util.makeRecursiveFileList(folder, filter);
        KafSaxParser kafSaxParser = new KafSaxParser();
        for (int i = 0; i < nafFiles.size(); i++) {
            File file = nafFiles.get(i);
            kafSaxParser.parseFile(file);
            String uri = kafSaxParser.getKafMetaData().getUrl();
            tokenMap.put(uri, kafSaxParser.kafWordFormList);
        }
    }

/*    public void writeNafToStream(OutputStream stream)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            Document xmldoc = impl.createDocument(null, "NAF", null);
            xmldoc.setXmlStandalone(false);
            Element root = xmldoc.getDocumentElement();
            root.setAttribute("version", NAFVERSION);
            root.setAttribute("xml:lang", kafMetaData.getLanguage());
            root.appendChild(kafMetaData.toNafHeaderXML(xmldoc));

            if (!rawText.isEmpty())  {
                Element text = xmldoc.createElement("raw");
                text.setTextContent(rawText);
                root.appendChild(text);
            }
            if (this.kafWordFormList.size()>0) {
                Element text = xmldoc.createElement("text");
                for (int i = 0; i < this.kafWordFormList.size(); i++) {
                    KafWordForm kaf  = kafWordFormList.get(i);
                    //System.out.println("kaf.getWf() = " + kaf.getWf());
                    text.appendChild(kaf.toNafXML(xmldoc));
                }
                root.appendChild(text);
            }

            if (this.kafTermList.size()>0) {
                Element terms = xmldoc.createElement("terms");
                for (int i = 0; i < this.kafTermList.size(); i++) {
                    KafTerm kaf  = kafTermList.get(i);
                    kaf.setTokenString(AddTokensAsCommentsToSpans.getTokenString(this, kaf.getSpans()));
                    terms.appendChild(kaf.toNafXML(xmldoc));
                }
                root.appendChild(terms);
            }

            if (this.kafDepList.size()>0) {
                Element deps = xmldoc.createElement("deps");
                for (int i = 0; i < this.kafDepList.size(); i++) {
                    KafDep kaf  = kafDepList.get(i);
                    String commentString = kaf.getTokensString(this);
                    Comment comment = xmldoc.createComment(commentString);
                    deps.appendChild(comment);
                    /// the next checks are needed because some parser create reference to nonexisting elements
                    if ((this.getTerm(kaf.from)!=null) && (this.getTerm(kaf.to)!=null)) {
                        deps.appendChild(kaf.toNafXML(xmldoc));
                    }
                }
                root.appendChild(deps);
            }

            if (this.kafChunkList.size()>0) {
                Element chunks = xmldoc.createElement("chunks");
                for (int i = 0; i < this.kafChunkList.size(); i++) {
                    KafChunk kaf  = kafChunkList.get(i);
                    kaf.setTokenString(this);
                    /// the next checks are needed because some parser create reference to nonexisting elements
                    boolean nullSpan = false;
                    for (int j = 0; j < kaf.getSpans().size(); j++) {
                        String span = kaf.getSpans().get(j);
                        if (this.getTerm(span)==null) {
                            nullSpan = true;
                            break;
                        }
                    }
                    if (this.getTerm(kaf.getHead())!=null) {
                        if (!nullSpan) chunks.appendChild(kaf.toNafXML(xmldoc));
                    }
                }
                root.appendChild(chunks);
            }

            if (kafOpinionArrayList.size()>0) {
                Element opinions = xmldoc.createElement("opinions");
                for (int i = 0; i < this.kafOpinionArrayList.size(); i++){
                    KafOpinion kaf  =  kafOpinionArrayList.get(i);
                    kaf.setTokenStrings(this);
                    opinions.appendChild(kaf.toNafXML(xmldoc));
                }
                root.appendChild(opinions);
            }

            if (kafEntityArrayList.size()>0) {
                Element entities = xmldoc.createElement("entities");
                for (int i = 0; i < this.kafEntityArrayList.size(); i++) {
                    KafEntity kaf  = kafEntityArrayList.get(i);
                    kaf.setTokenStrings(this);
                    entities.appendChild(kaf.toNafXML(xmldoc));
                }
                root.appendChild(entities);
            }

            if (kafPropertyArrayList.size()>0) {
                Element properties = xmldoc.createElement("properties");
                for (int i = 0; i < this.kafPropertyArrayList.size(); i++) {
                    KafProperty kaf  = kafPropertyArrayList.get(i);
                    kaf.setTokenStrings(this);
                    properties.appendChild(kaf.toNafXML(xmldoc));
                }
                root.appendChild(properties);
            }

            if (kafCorefenceArrayList.size()>0) {
                Element coreferences = xmldoc.createElement("coreferences");
                for (int i = 0; i < this.kafCorefenceArrayList.size(); i++) {
                    KafCoreferenceSet kaf  = kafCorefenceArrayList.get(i);
                    kaf.setTokenStrings(this);
                    coreferences.appendChild(kaf.toNafXML(xmldoc));
                }
                root.appendChild(coreferences);
            }

            if (kafConstituencyTrees.size()>0) {
                Element constituency = xmldoc.createElement("constituency");
                for (int i = 0; i < kafConstituencyTrees.size(); i++) {
                    KafConstituencyTree constituencyTree = kafConstituencyTrees.get(i);
                    constituencyTree.addComments(this);
                    constituency.appendChild(constituencyTree.toNafXML(xmldoc));
                }
                root.appendChild(constituency);
            }
            if (kafCountryArrayList.size()>0) {
                Element locations = xmldoc.createElement("locations");
                for (int i = 0; i < kafCountryArrayList.size(); i++) {
                    GeoCountryObject geoCountryObject = kafCountryArrayList.get(i);
                    locations.appendChild(geoCountryObject.toNafXML(xmldoc));
                }
                for (int i = 0; i < kafPlaceArrayList.size(); i++) {
                    GeoPlaceObject geoPlaceObject = kafPlaceArrayList.get(i);
                    locations.appendChild(geoPlaceObject.toNafXML(xmldoc));
                }
                root.appendChild(locations);
            }

            if (kafDateArrayList.size()>0) {
                Element dates = xmldoc.createElement("dates");
                for (int i = 0; i < kafDateArrayList.size(); i++) {
                    ISODate isoDate = kafDateArrayList.get(i);
                    dates.appendChild(isoDate.toNafXML(xmldoc));
                }
                root.appendChild(dates);
            }


            if (kafEventArrayList.size()>0) {
                Element events  = xmldoc.createElement("srl");

                for (int i = 0; i < kafEventArrayList.size(); i++) {
                    KafEvent event = kafEventArrayList.get(i);
                    event.setTokenString(AddTokensAsCommentsToSpans.getTokenStringFromTermIds(this, event.getSpanIds()));
                    for (int j = 0; j < event.getParticipants().size(); j++) {
                        KafParticipant kafParticipant = event.getParticipants().get(j);
                        kafParticipant.setTokenString(AddTokensAsCommentsToSpans.getTokenStringFromTermIds(this, kafParticipant.getSpanIds()));
                    }
                    events.appendChild(event.toNafXML(xmldoc));
                }
                root.appendChild(events);
            }


            if (kafTimexLayer.size()>0) {
                Element timexLayer = xmldoc.createElement("timeExpressions");

                for (int i = 0; i < kafTimexLayer.size(); i++) {
                    KafTimex timex = kafTimexLayer.get(i);
                    timex.setTokenString(AddTokensAsCommentsToSpans.getTokenString(this, timex.getSpans()));
                    timexLayer.appendChild(timex.toNafXML(xmldoc));
                }

                root.appendChild(timexLayer);
            }

            if (kafClinks.size()>0) {
                Element clinksLayer = xmldoc.createElement("causalRelations");

                for (int i = 0; i < kafClinks.size(); i++) {
                    KafEventRelation eventRelation = kafClinks.get(i);
                    clinksLayer.appendChild(eventRelation.toNafXML(xmldoc, "clink"));
                }

                root.appendChild(clinksLayer);
            }

            if (kafTlinks.size()>0 || kafPredicateAnchorArrayList.size()>0) {
                Element tlinksLayer = xmldoc.createElement("temporalRelations");

                for (int i = 0; i < kafTlinks.size(); i++) {
                    KafEventRelation eventRelation = kafTlinks.get(i);
                    tlinksLayer.appendChild(eventRelation.toNafXML(xmldoc, "tlink"));
                }

                for (int i = 0; i < kafPredicateAnchorArrayList.size(); i++) {
                    KafPredicateAnchor predicateAnchor = kafPredicateAnchorArrayList.get(i);
                    tlinksLayer.appendChild(predicateAnchor.toNafXML(xmldoc));
                }
                root.appendChild(tlinksLayer);
            }

            if (kafFactualityLayer.size()>0) {
                Element factualities = xmldoc.createElement("factualitylayer");

                for (int i = 0; i < kafFactualityLayer.size(); i++) {
                    KafFactuality kafFactuality = kafFactualityLayer.get(i);
                    kafFactuality.setTokenString(this);
                    factualities.appendChild((kafFactuality.toNafXML(xmldoc)));
                }
                root.appendChild(factualities);
            }

            if (kafAttributionArrayList.size()>0) {
                Element factualities = xmldoc.createElement("attribution");

                for (int i = 0; i < kafAttributionArrayList.size(); i++) {
                    KafStatement kafStatement = kafAttributionArrayList.get(i);
                    kafStatement.setTokenString(this);
                    factualities.appendChild((kafStatement.toNafXML(xmldoc)));
                }
                root.appendChild(factualities);
            }

            if (kafTopicsArrayList.size()>0) {
                Element topics = xmldoc.createElement("topics");

                for (int i = 0; i < kafTopicsArrayList.size(); i++) {
                    KafTopic kafTopic = kafTopicsArrayList.get(i);
                    topics.appendChild((kafTopic.toNafXML(xmldoc)));
                }
                root.appendChild(topics);
            }

            if (kafMarkablesArrayList.size()>0) {
                Element markables = xmldoc.createElement("markables");

                for (int i = 0; i < kafMarkablesArrayList.size(); i++) {
                    KafMarkable kafMarkable = kafMarkablesArrayList.get(i);
                    kafMarkable.setTokenString(this);
                    markables.appendChild((kafMarkable.toNafXML(xmldoc)));
                }
                root.appendChild(markables);
            }

*//*       @deprecated
            Element tunits = xmldoc.createElement("tunits");
            for (int i = 0; i < this.kafDiscourseList.size(); i++) {
                KafTextUnit kaf  =  kafDiscourseList.get(i);
                tunits.appendChild(kaf.toNafXML(xmldoc));
            }
            root.appendChild(tunits);

*//*
            // Serialisation through Tranform.
            DOMSource domSource = new DOMSource(xmldoc);
            TransformerFactory tf = TransformerFactory.newInstance();
            //tf.setAttribute("indent-number", 4);
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT,"yes");
            //serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            //serializer.setParameter("format-pretty-print", Boolean.TRUE);
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StreamResult streamResult = null;
            if (encoding.isEmpty()) {
                streamResult = new StreamResult(new OutputStreamWriter(stream));
            }
            else {
                streamResult = new StreamResult(new OutputStreamWriter(stream, encoding));
            }
            serializer.transform(domSource, streamResult);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }*/

}
