package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.util.TimeLanguage;

import java.io.Serializable;

/**
 * Created by piek on 4/10/14.
 */
public class OwlTime implements Serializable {



         /*
             nwr:20010101
        owltime:day "1"^^xsd:int ;
        owltime:month "1"^^xsd:int ;
        owltime:year "2001"^^xsd:int .
          */

   private String instance;
   private String day;
   private String month;
   private String year;

   public  OwlTime () {
        this.instance = "";
        this.day = "";
        this.month = "";
        this.year = "";
   }


    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String separateWords (String date) {
        String str = date;
        String [] fields = date.split(" ");
        if (fields.length>1) {
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];
                str += " "+removePunctuation(field);
            }
        }
        return str;
    }

    public String removePunctuation (String s) {
        final String punctuation = ",./\\;(){}[]:";
        String str = "";
        for (int i = 0; i < s.toCharArray().length; i++) {
            String c = s.substring(i, i+1);
            if (punctuation.contains(c)) {
                str += " ";
            }
            else {
                str += c;
            }
        }
        return str.trim();
    }

    public void parsePublicationDate (String date) {
        ///2013-01-01
        try {
            if (date.length()==8) {
                this.year = date.substring(0,4);
                this.month = date.substring(4,6);
                this.day = date.substring(6,8);
            }
            else {
                String[] fields = date.split("-");
                if (fields.length == 3) {
                    //System.out.println("date = " + date);
                    this.instance = date;
                    if (fields[0].length() == 4) {
                        String dayString = fields[2];
                        if (dayString.indexOf("T")>-1)  {
                            //2007-10-16T00:00:00Z
                           dayString = dayString.substring(0, 2);
                        }
                        this.day = (new Integer(dayString).toString());
                        this.month = (new Integer(fields[1])).toString();
                        this.year = (new Integer(fields[0])).toString();
                    } else if (fields[2].length() == 4) {
                        this.day = (new Integer(fields[0])).toString();
                        this.month = (new Integer(fields[1])).toString();
                        this.year = (new Integer(fields[2])).toString();
                    }
                } else {
                    /// publication dates can have all kinds of formats.
                    // November 18, 2004
                    parseStringDate(date);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("date = " + date);
        }
    }

    public boolean matchTimeExact (OwlTime time) {
        if (time.getDateString().isEmpty() || this.getDateString().isEmpty()) {
            return false;
        }
        if (time.getDateString().equals(this.getDateString())) {
            return true;
        }
        else {
           return false;
        }
    }

    public boolean matchTimeEmbedded (OwlTime time) {
        if (time.getDateString().isEmpty() || this.getDateString().isEmpty()) {
            return false;
        }
        if (time.getDateString().equals(this.getDateString())) {
            return true;
        }
        else {
            if (!time.getYear().equals(this.getYear())) {
                return false;
            } else {
                if (time.getDay().isEmpty() || this.getDay().isEmpty()) {
                    if (time.getMonth().equals(this.getMonth())) {
                        return true;
                    } else if (time.getMonth().isEmpty() || this.getMonth().isEmpty()) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }

    public int parseTimeExValue (String timeExValue, OwlTime docOwlTime) {
        int foundTime = -1;
        if (timeExValue.equalsIgnoreCase("xx-xx-xx")) {
            return foundTime;
        }
        if (timeExValue.equalsIgnoreCase("xxxx-xx-xx")) {
            return foundTime;
        }
        String [] fields = timeExValue.split("-");
        if (fields.length == 3) {
            //System.out.println("date = " + date);
            this.instance = timeExValue;
            if (fields[0].length() == 4) {
                if (!fields[2].equalsIgnoreCase("xx")) this.day = (new Integer(fields[2])).toString();
                if (!fields[1].equalsIgnoreCase("xx")) this.month = (new Integer(fields[1])).toString();
                if (!fields[0].equalsIgnoreCase("xxxx")) this.year = (new Integer(fields[0])).toString();
            } else if (fields[2].length() == 4) {
                if (!fields[0].equalsIgnoreCase("xx")) this.day = (new Integer(fields[0])).toString();
                if (!fields[1].equalsIgnoreCase("xx")) this.month = (new Integer(fields[1])).toString();
                if (!fields[2].equalsIgnoreCase("xxxx"))this.year = (new Integer(fields[2])).toString();
            }
            foundTime = 1;
        }
        return foundTime;
    }

    public int parseStringDateWithDocTimeYearFallBack(String rawdate, OwlTime docOwlTime) {
        int foundTime = -1;
        try {
            String date = removePunctuation(rawdate);
            String year = TimeLanguage.getYearFromString(date);
            if (!year.isEmpty()) {
                this.year = year;
                foundTime = 1;
                int month = TimeLanguage.getMonthFromString(date);
                if (month>-1) {
                    this.month = (new Integer(month)).toString();
                    int day = TimeLanguage.getDayFromString(date);
                    if (day>-1) {
                        this.day = (new Integer(day).toString());
                    }
                }
            }
            else {
                if (!docOwlTime.getYear().isEmpty()) {
                    int month = TimeLanguage.getMonthWordFromString(date);
                    if (month>-1) {
                        foundTime = 1;
                        this.year = docOwlTime.getYear();
                        this.month = (new Integer(month)).toString();
                        if (this.month.length()==1) {
                            this.month = "0"+this.month;
                        }
                        int day = TimeLanguage.getDayFromString(date);
                        if (day>-1) {
                            this.day = (new Integer(day).toString());
                            if (this.day.length()==1) {
                                this.day = "0"+this.day;
                            }
                        }
                    }
                    else {
/*                        int day = getDayFromString(date);
                        if (day>-1) {
                            foundTime = 1;
                            this.year = docOwlTime.getYear();
                            this.month = docOwlTime.getMonth();
                            this.day = (new Integer(day).toString());
                        }*/
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("rawdate = " + rawdate);
        }
        return foundTime;
    }

    public void parseStringDate (String rawdate) {
        try {
            String date = removePunctuation(rawdate);
            String year = TimeLanguage.getYearFromString(date);
            if (!year.isEmpty()) {
                this.year = year;
                int month = TimeLanguage.getMonthFromString(date);
                if (month>-1) {
                    this.month = (new Integer(month)).toString();
                    int day = TimeLanguage.getDayFromString(date);
                    if (day>-1) {
                        this.day = (new Integer(day).toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
          //  System.out.println("rawdate = " + rawdate);
        }
    }


    public String getDateString () {
        String str = ResourcesUri.nwrtime+this.year;
        if (!this.month.isEmpty()) {
            if (this.month.length()==1) {
                str +="0";
            }
            str += month;
            if (!this.day.isEmpty()) {
                if (day.length()==1) {
                    str += "0";
                }
                str += day;
            }
        }
        return str;
    }

    public void addToJenaModelOwlTimeInstant (Model model) {
                 /*
             nwr:20010101
        owltime:day "1"^^xsd:int ;
        owltime:month "1"^^xsd:int ;
        owltime:year "2001"^^xsd:int .
          */

        Resource resource = model.createResource(this.getDateString());
        Property property = model.createProperty(ResourcesUri.owltime+"DateTimeDescription");

        resource.addProperty(RDF.type, property);

        if (!this.day.isEmpty()) {
            Property day = model.createProperty(ResourcesUri.owltime+"day");
            resource.addProperty(day, this.getDay(), XSDDatatype.XSDgDay);
        }
        if (!this.month.isEmpty()) {
            Property month = model.createProperty(ResourcesUri.owltime+"month");
            resource.addProperty(month, this.getMonth(),XSDDatatype.XSDgMonth);
        }
        if (!this.year.isEmpty()) {
            Property year = model.createProperty(ResourcesUri.owltime+"year");
            resource.addProperty(year, this.getYear(),XSDDatatype.XSDgYear);
            Property unit = model.createProperty(ResourcesUri.owltime+"unitType");
            Property day = model.createProperty(ResourcesUri.owltime+"unitDay");
            resource.addProperty(unit, day);
        }
    }

    public void addToJenaModelOwlTimeDuration (Model model) {
                 /*
             nwr:20010101
        owltime:day "1"^^xsd:int ;
        owltime:month "1"^^xsd:int ;
        owltime:year "2001"^^xsd:int .
          */

        Resource resource = model.createResource(this.getDateString());
        Property property = model.createProperty(ResourcesUri.owltime+"DurationDescription");

        resource.addProperty(RDF.type, property);

        if (!this.day.isEmpty()) {
            Property day = model.createProperty(ResourcesUri.owltime+"day");
            resource.addProperty(day, this.getDay(), XSDDatatype.XSDgDay);
        }
        if (!this.month.isEmpty()) {
            Property month = model.createProperty(ResourcesUri.owltime+"month");
            resource.addProperty(month, this.getMonth(),XSDDatatype.XSDgMonth);
        }
        if (!this.year.isEmpty()) {
            Property year = model.createProperty(ResourcesUri.owltime+"year");
            resource.addProperty(year, this.getYear(),XSDDatatype.XSDgYear);
            Property unit = model.createProperty(ResourcesUri.owltime+"unitType");
            Property day = model.createProperty(ResourcesUri.owltime+"unitDay");
            resource.addProperty(unit, day);
        }
    }

    public String toString () {
        String str = this.year+"-";
        if (this.month.length()==1) {
            str += "0";
        }
        str += this.month+"-";
        if (this.day.length()==1) {
            str += "0";
        }
        str += this.day;
        return str;
    }

}
