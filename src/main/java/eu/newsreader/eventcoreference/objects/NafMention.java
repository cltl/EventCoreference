package eu.newsreader.eventcoreference.objects;

import eu.kyotoproject.kaf.KafFactuality;

import java.util.ArrayList;

/**
 * Created by piek on 1/22/14.
 */
public class NafMention {
    private String baseUri;
    private String offSetStart;
    private String offSetEnd;
    private ArrayList<String> tokensIds;
    private ArrayList<String> termsIds;
    private KafFactuality factuality;


    public NafMention(String baseUri) {
        this.baseUri = baseUri;
        this.offSetStart = "";
        this.offSetEnd = "";
        this.tokensIds = new ArrayList<String>();
        this.termsIds = new ArrayList<String>();
        this.factuality = new KafFactuality();
    }

    public NafMention() {
        this.baseUri = "";
        this.offSetStart = "";
        this.offSetEnd = "";
        this.tokensIds = new ArrayList<String>();
        this.termsIds = new ArrayList<String>();
        this.factuality = new KafFactuality();
    }

    public KafFactuality getFactuality() {
        return factuality;
    }

    public void setFactuality(KafFactuality factuality) {
        this.factuality = factuality;
    }

    public String getBaseUri() {
        return baseUri;
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

    public String toString () {
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
        return str;
    }
}
