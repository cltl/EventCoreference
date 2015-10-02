package eu.newsreader.eventcoreference.objects;

import eu.kyotoproject.kaf.KafFactuality;
import eu.kyotoproject.kaf.KafOpinion;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafWordForm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by piek on 1/22/14.
 */
public class NafMention implements Serializable {
    private String baseUri;
    private String phrase;
    private String sentence;
    private String sentenceText;
    private String offSetStart;
    private String offSetEnd;
    private ArrayList<String> tokensIds;
    private ArrayList<String> termsIds;
    private ArrayList<KafFactuality> factualities;
    private ArrayList<KafOpinion> opinions;


    public NafMention(String baseUri) {
        this.baseUri = baseUri;
        this.phrase = "";
        this.offSetStart = "";
        this.offSetEnd = "";
        this.sentence = "";
        this.sentenceText = "";
        this.tokensIds = new ArrayList<String>();
        this.termsIds = new ArrayList<String>();
        this.factualities = new ArrayList<KafFactuality>();
        this.opinions = new ArrayList<KafOpinion>();
    }

    public NafMention() {
        this.baseUri = "";
        this.phrase = "";
        this.offSetStart = "";
        this.offSetEnd = "";
        this.sentence = "";
        this.sentenceText = "";
        this.tokensIds = new ArrayList<String>();
        this.termsIds = new ArrayList<String>();
        this.factualities = new ArrayList<KafFactuality>();
        this.opinions = new ArrayList<KafOpinion>();
    }

    public String getPhraseFromMention (KafSaxParser kafSaxParser) {
        String str = "";
        for (int i = 0; i < tokensIds.size(); i++) {
            String s = tokensIds.get(i);
            KafWordForm kafWordForm = kafSaxParser.getWordForm(s);
            if (kafWordForm!=null) {
                str += kafWordForm.getWf()+" ";
            }
        }
        return str.trim();
    }

    public void setPhraseFromMention (KafSaxParser kafSaxParser) {
        String str = "";
        for (int i = 0; i < tokensIds.size(); i++) {
            String s = tokensIds.get(i);
            KafWordForm kafWordForm = kafSaxParser.getWordForm(s);
            if (kafWordForm!=null) {
                str += kafWordForm.getWf()+" ";
            }
        }
        this.phrase = str.trim();
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(KafSaxParser kafSaxParser) {
        for (int i = 0; i < tokensIds.size(); i++) {
            String s = tokensIds.get(i);
            KafWordForm kafWordForm = kafSaxParser.getWordForm(s);
            if (kafWordForm!=null) {
                //System.out.println(kafWordForm.getSent()+" = " + kafWordForm.getWf());
                sentence = kafWordForm.getSent();
                break;
            }
        }
    }

    public void setSentenceText(KafSaxParser kafSaxParser) {
        for (int i = 0; i < tokensIds.size(); i++) {
            String s = tokensIds.get(i);
            KafWordForm kafWordForm = kafSaxParser.getWordForm(s);
            if (kafWordForm!=null) {
                //System.out.println(kafWordForm.getSent()+" = " + kafWordForm.getWf());
                if (sentence.equals(kafWordForm.getSent())) {
                   if (!sentenceText.isEmpty()) {
                        sentenceText+=" ";
                    }
                    sentenceText+= kafWordForm.getWf();
                }
            }
        }
    }

    public String getPhrase() {
        return phrase;
    }

    public ArrayList<KafFactuality> getFactuality() {
        return factualities;
    }

    public void addFactuality(KafSaxParser kafSaxParser) {
        for (int i = 0; i < kafSaxParser.kafFactualityLayer.size(); i++) {
            KafFactuality kafFactuality = kafSaxParser.kafFactualityLayer.get(i);
                /// in naf.v2 factuality uses tokens as span, in naf.v3 factuality uses terms as spans
            if (!Collections.disjoint(termsIds, kafFactuality.getSpans())) {
                    this.factualities.add(kafFactuality);
             //   System.out.println("kafFactuality.getPrediction() = " + kafFactuality.getPrediction());
             //   System.out.println("this.factualities.size() = " + this.factualities.size());
            }
        }
    }

    public ArrayList<KafOpinion> getOpinions() {
        return opinions;
    }

    public void addOpinion(KafSaxParser kafSaxParser) {
        for (int i = 0; i < kafSaxParser.kafOpinionArrayList.size(); i++) {
            KafOpinion kafOpinion = kafSaxParser.kafOpinionArrayList.get(i);
            if (!Collections.disjoint(termsIds, kafOpinion.getSpansOpinionTarget())) {
                this.opinions.add(kafOpinion);
            }
            if (!Collections.disjoint(termsIds, kafOpinion.getSpansOpinionExpression())) {
                this.opinions.add(kafOpinion);
            }
        }
    }

    public String getBaseUri() {
        return baseUri;
    }

    public String getBaseUriWithoutId() {
        int idx = baseUri.lastIndexOf("#");
        if (idx>-1) {
            return baseUri.substring(0, idx);
        }
        else {
            return baseUri;
        }
    }

    public String getIdFromBaseUri() {
            int idx = baseUri.lastIndexOf("#");
            if (idx>-1) {
                return baseUri.substring(idx+1);
            }
            else {
                return baseUri;
            }
        }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getOffSetStart() {
        return offSetStart;
    }

    public void setOffSetStart(String offSetStart) {
        this.offSetStart = offSetStart;
    }

    public String getOffSetEnd() {
        return offSetEnd;
    }

    public void setOffSetEnd(String offSetEnd) {
        this.offSetEnd = offSetEnd;
    }

    public ArrayList<String> getTokensIds() {
        return tokensIds;
    }

    public void setTokensIds(ArrayList<String> tokensIds) {
        this.tokensIds = tokensIds;
    }

    public void addTokensId(String tokensId) {
        this.tokensIds.add(tokensId);
    }

    public ArrayList<String> getTermsIds() {
        return termsIds;
    }

    public void setTermsIds(ArrayList<String> termsIds) {
        this.termsIds = termsIds;
    }
    public void addTermsId(String termsId) {
        this.termsIds.add(termsId);
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getSentenceText() {
        return sentenceText;
    }

    public void setSentenceText(String sentenceText) {
        this.sentenceText = sentenceText;
    }

    public String toString () {
        String str = baseUri;
        if (!offSetStart.isEmpty() && !offSetEnd.isEmpty())  {
            str +="char="+this.offSetStart+","+this.offSetEnd;
        }
        return str;
    }

    public String toStringFull () {
        String str = baseUri;
        if (!offSetStart.isEmpty() && !offSetEnd.isEmpty())  {
            str +="char="+this.offSetStart+","+this.offSetEnd;
        }
        if (tokensIds.size()>0) {
            str += "&word=";
            for (int i = 0; i < tokensIds.size(); i++) {
                String s = tokensIds.get(i);
                if (i==0) str += s;
                else str += ","+s;
            }
        }
        if (termsIds.size()>0) {
            str +="&term=";
            for (int i = 0; i < termsIds.size(); i++) {
                String s = termsIds.get(i);
                if (i==0) str += s;
                else str += ","+s;

            }
        }
        if (!sentence.isEmpty())  {
            str += "&sentence="+sentence;
        }
        return str;
    }

    public boolean sameMention (NafMention mention) {
        if (!this.getBaseUri().equals(mention.getBaseUri())) {
            return false;
        }
        if (!this.getOffSetStart().equals(mention.getOffSetStart())) {
            return false;
        }
        if (!this.getOffSetEnd().equals(mention.getOffSetEnd())) {
            return false;
        }
        for (int i = 0; i < tokensIds.size(); i++) {
            String tokenId = tokensIds.get(i);
            if (!mention.getTokensIds().contains(tokenId)) {
                return false;
            }
        }
        for (int i = 0; i < mention.getTokensIds().size(); i++) {
            String tokenId = mention.getTokensIds().get(i);
            if (!this.getTokensIds().contains(tokenId)) {
                return false;
            }
        }
        for (int i = 0; i < termsIds.size(); i++) {
            String termId = termsIds.get(i);
            if (!mention.getTermsIds().contains(termId)) {
                return false;
            }
        }
        for (int i = 0; i < mention.getTermsIds().size(); i++) {
            String termId = mention.getTermsIds().get(i);
            if (!this.getTermsIds().contains(termId)) {
                return false;
            }
        }
        return true;
    }

    public boolean sameMentionForDifferentSource (NafMention mention) {
        if (!this.getBaseUri().equals(mention.getBaseUri())) {
         //   System.out.println("mention.toString() = " + mention.toString());
         //   System.out.println("this.toString() = " + this.toString());
            if (!this.getOffSetStart().equals(mention.getOffSetStart())) {
                return false;
            }
            if (!this.getOffSetEnd().equals(mention.getOffSetEnd())) {
                return false;
            }
            for (int i = 0; i < tokensIds.size(); i++) {
                String tokenId = tokensIds.get(i);
                if (!mention.getTokensIds().contains(tokenId)) {
                    return false;
                }
            }
            for (int i = 0; i < mention.getTokensIds().size(); i++) {
                String tokenId = mention.getTokensIds().get(i);
                if (!this.getTokensIds().contains(tokenId)) {
                    return false;
                }
            }
            for (int i = 0; i < termsIds.size(); i++) {
                String termId = termsIds.get(i);
                if (!mention.getTermsIds().contains(termId)) {
                    return false;
                }
            }
            for (int i = 0; i < mention.getTermsIds().size(); i++) {
                String termId = mention.getTermsIds().get(i);
                if (!this.getTermsIds().contains(termId)) {
                    return false;
                }
            }
            return true;
        }
        else {
       //     System.out.println("mention.getBaseUri() = " + mention.getBaseUri());
            return false;
        }
    }

    public boolean hasMention (ArrayList<NafMention> mentions) {
        for (int i = 0; i < mentions.size(); i++) {
            NafMention nafMention = mentions.get(i);
            if (this.sameMention(nafMention)) {
                return true;
            }
        }
        return  false;
    }
}
