package eu.newsreader.eventcoreference.ontology.thesis;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by piek on 9/12/14.
 */
public class RdfSense {
    private static int PARENT 	= 0;
    private static int ABOUT 	= 1;
    private static int LABEL	= 2;
    private static int COMMENT	= 3;
    private static int STRING_ARRAY_SIZE = 4;
    private ArrayList<String[]> rdfList = new ArrayList<String[]>();

    private void start(){
        //read the file into rdfList
        //todo parse rdfList into tree (based on parent index)
        readRdf("/Users/michakemeling/Dropbox/TH@SIS/thesaurus/Owled/rdfTester.owl");
    }

    public RdfSense(String s){
        readRdf(s);
    }

    public ArrayList<String[]> getRdfData(){
        return rdfList;
    }

    private void readRdf(String fileLocation){
        String removeUselessStuff = " [Object]";
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(fileLocation);
        try{
            Document d =  builder.build(xmlFile);
            Element rootNode = d.getRootElement();
            //get eu.kyotoproject.rdf namespace object from root
            org.jdom.Namespace rdfNamespace = rootNode.getNamespace("eu/kyotoproject/rdf");
            //all children of root
            List classList = rootNode.getChildren();
            Element owlClass;
            List classInfo;
            Element singleElement;

            for(int i = 0;i< classList.size(); i++){
                owlClass = (Element) classList.get(i);
                //only parse <owl:class> tags
                if(owlClass.getName().equals("Class")){
                    classInfo = owlClass.getChildren();
                    String[] temp = new String[STRING_ARRAY_SIZE];
                    Arrays.fill(temp, null);
                    String about = owlClass.getAttributeValue("about", rdfNamespace);
                    temp[ABOUT] = about.replace(removeUselessStuff, "");


                    for(int j = 0; j<classInfo.size(); j++){
                        singleElement = (Element) classInfo.get(j);
                        String value;
                        if(singleElement.getName().equals("label")){
                            value = singleElement.getValue();
                            temp[LABEL] = value.replace(removeUselessStuff, "");
                        }
                        if(singleElement.getName().equals("subClassOf")){
                            value = singleElement.getAttributeValue("resource", rdfNamespace);
                            temp[PARENT] = value.replace(removeUselessStuff, "");
                        }
                        if(singleElement.getName().equals("comment")){
                            value = singleElement.getValue();
                            temp[COMMENT] = value.replace(removeUselessStuff, "");
                        }
                    }
                    rdfList.add(temp);
                }
            }
        } catch (IOException io){
            System.out.println(io.getMessage());
            System.exit(0);
        } catch (JDOMException jEx){
            System.out.println(jEx.getMessage());
            System.exit(1);
        }
    }
}
