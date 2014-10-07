package eu.newsreader.eventcoreference.ontology.thesis;

/**
 * Created by piek on 9/12/14.
 */
public class NodeData {
    private String url;
    private String label;
    private String comment;


    public NodeData(){
        this.url 	= null;
        this.label 	= null;
        this.comment= null;
    }

    public NodeData(String u, String l, String c){
        this.url 	= (u!=null) ? u:"";
        this.label 	= (l!=null) ? l:"";;
        this.comment= (c!=null) ? c:"";;
    }

    public String getUrl(){
        return this.url;
    }

    public String getLabel(){
        return this.label;
    }

    public String getComment(){
        return this.comment;
    }

}
