package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 1/16/14.
 */
public class LabelCount {
    private String label;
    private Integer cnt;

    public LabelCount(String label, Integer cnt) {
        this.label = label;
        this.cnt = cnt;
    }

    public LabelCount() {
        this.label = label;
        this.cnt = cnt;
    }

    public LabelCount(String labelWithCount) {
        String[] fields = labelWithCount.split(":");
        if (fields.length==2) {
            this.label = fields[0].trim();
            this.cnt = Integer.parseInt(fields[1].trim());
        }
        else {
            this.label = "";
            this.cnt = 0;
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    public void addCnt(Integer cnt) {
        this.cnt += cnt;
    }
}
