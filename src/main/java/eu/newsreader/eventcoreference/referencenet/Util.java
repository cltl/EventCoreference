package eu.newsreader.eventcoreference.referencenet;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.newsreader.eventcoreference.objects.PhraseCount;

import java.util.*;

/**
 * Created by piek on 22/10/2017.
 */
public class Util {

    static SortedSet<PhraseCount> freqSortPhraseCountArrayList(ArrayList<PhraseCount> counts) {
        SortedSet<PhraseCount> treeSet = new TreeSet<PhraseCount>(new PhraseCount.Compare());
        for (PhraseCount phraseCount : counts) {
            boolean match = false;
            for (PhraseCount cumMention : treeSet) {
                if (phraseCount.getPhrase().equals(cumMention.getPhrase())) {
                    cumMention.addCount(phraseCount.getCount());
                    match = true;
                    break;
                }
            }
            if (!match) {
                treeSet.add(phraseCount);
            }
        }
        return treeSet;
    }

    static SortedSet<PhraseCount> formSortPhraseCountArrayList(ArrayList<PhraseCount> counts) {
        SortedSet<PhraseCount> treeSet = new TreeSet<PhraseCount>(new PhraseCount.ComparePhrase());
        for (PhraseCount phraseCount : counts) {
            boolean match = false;
            for (PhraseCount cumMention : treeSet) {
                if (phraseCount.getPhrase().equals(cumMention.getPhrase())) {
                    cumMention.addCount(phraseCount.getCount());
                    match = true;
                    break;
                }
            }
            if (!match) {
                treeSet.add(phraseCount);
            }
        }
        return treeSet;
    }


    static SortedSet<PhraseCount> sortPhraseCounts (ArrayList<PhraseCount> pcounts) {
        SortedSet<PhraseCount> treeSet = new TreeSet<PhraseCount>(new PhraseCount.Compare());
        for (PhraseCount phraseCount : pcounts) {
            treeSet.add(phraseCount);
        }
        return treeSet;
    }

    static void initStandfordCoreNLP (StanfordCoreNLP pipeline) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        pipeline = new StanfordCoreNLP(props);

    }

    static String getLemmaCoreNLP (StanfordCoreNLP pipeline, String form) {
        String tokenLemma =form;
        //System.out.println("form = " + form);
        Annotation tokenAnnotation = new Annotation(form);
        if (tokenAnnotation == null) {
            //System.out.println("form = " + form);
        } else {
            try {
                pipeline.annotate(tokenAnnotation);  // necessary for the LemmaAnnotation to be set.
                List<CoreMap> list = tokenAnnotation.get(CoreAnnotations.SentencesAnnotation.class);
                tokenLemma = list
                        .get(0).get(CoreAnnotations.TokensAnnotation.class)
                        .get(0).get(CoreAnnotations.LemmaAnnotation.class);
            } catch (Exception e) {
                //e.printStackTrace();
                //System.out.println("form = " + form);
            }

        }
        return tokenLemma;
    }

    static void getAnnotationsCoreNLP (StanfordCoreNLP pipeline, String form) {
        Annotation document = new Annotation(form);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        /*for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                System.out.println("word: " + word + " pos: " + pos + " ne:" + ne);
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            System.out.println("parse tree:\n" + tree);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println("dependency graph:\n" + dependencies);
        }*/

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph =
                document.get(CorefCoreAnnotations.CorefChainAnnotation.class);

    }
}
