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
       @TODO remove serialVersionUID
    */
    //java.io.InvalidClassException: eu.newsreader.eventcoreference.objects.OwlTime;
    // local class incompatible: stream classdesc serialVersionUID = 3238395448961710768, local class serialVersionUID = -3825371661027150878
    //stream classdesc serialVersionUID = -3825371661027150878, local class serialVersionUID = 3238395448961710768
    //stream classdesc serialVersionUID = -3825371661027150878, local class serialVersionUID = 3238395448961710768
   // stream classdesc serialVersionUID = -3825371661027150878, local class serialVersionUID = 3825371661027150878
   // private static final long serialVersionUID = -3825371661027150878L;
    private static final long serialVersionUID = 3238395448961710768L;


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

    public void birthOfJC () {
        this.instance = "0000-12-25";
        this.day = "25";
        this.month = "12";
        this.year = "0000";
    }

    public String getDay() {
        return day;
    }
    public String getDayValue() {
        String str = "---";
        if (day.length()==1) {
            str += "0";
        }
        str += day;
        return str;
    }

    public String getMonthValue() {
        String str = "--";
        if (month.length()==1) {
            str += "0";
        }
        str += month;
        return str;
    }
    /**
     * if (!this.month.isEmpty()) {
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
     * @param day
     */
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
       // System.out.println("date = " + date);
        try {
            if (date.length()==8) {
                this.year = date.substring(0,4);
                this.month = date.substring(4,6);
                this.day = date.substring(6,8);
            }
            else {
                //03-02-2009T00:00:00
                //2009-03-02T00:00:00
                String[] fields = date.split("-");
                if (fields.length == 3) {
                    //System.out.println("date = " + date);
                    this.instance = date;
                    if (fields[0].length() == 4) {
                        /// this must be the year
                        String dayString = fields[2];
                        if (dayString.indexOf("T")>-1)  {
                            //2007-10-16T00:00:00Z
                           dayString = dayString.substring(0, 2);
                        }
                        this.day = (new Integer(dayString).toString());
                        this.month = (new Integer(fields[1])).toString();
                        this.year = (new Integer(fields[0])).toString();
                    }
                    else if (fields[2].length() == 4) {
                        // this must be the year
                        this.day = (new Integer(fields[0])).toString();
                        this.month = (new Integer(fields[1])).toString();
                        this.year = (new Integer(fields[2])).toString();
                    }
                    else if (fields[2].length() > 4) {
                        // this must be the year
                        this.day = (new Integer(fields[0])).toString();
                        this.month = (new Integer(fields[1])).toString();
                        String yearString = fields[2];
                        if (yearString.indexOf("T")>-1)  {
                            //03-02-2009T00:00:00
                            yearString = yearString.substring(0, 4);
                        }
                        this.year = (new Integer(yearString)).toString();
                     //   System.out.println("yearString = " + yearString);
                    }
                } else {
                    /// publication dates can have all kinds of formats.
                    // November 18, 2004
                    parseStringDate(date);
                }
            }
        } catch (NumberFormatException e) {
           // e.printStackTrace();
           // System.out.println("date = " + date);
        }
    }

    public boolean matchTimeExact (OwlTime time) {
        if (time.getDateStringURI().isEmpty() || this.getDateStringURI().isEmpty()) {
            return false;
        }
        if (time.getDateStringURI().equals(this.getDateStringURI())) {
            return true;
        }
        else {
           return false;
        }
    }

    public boolean matchTimeEmbeddedOrg (OwlTime time) {
        if (time.getDateStringURI().isEmpty() || this.getDateStringURI().isEmpty()) {
            return false;
        }
        if (time.getDateStringURI().equals(this.getDateStringURI())) {
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
                    if (time.getDay().equals(this.getDay())) {
                        if (!time.getMonth().isEmpty() && !this.getMonth().isEmpty()) {
                            if (time.getMonth().equals(this.getMonth())) {
                                return true;
                            }
                            else {
                                return false;
                            }
                        }
                    }
                    return false;
                }
            }
        }
    }

    public boolean matchTimeEmbedded (OwlTime time) {
        if (!time.getYear().equals(this.getYear())) {
            return false;
        }

        //// at this point year, month and day are the same but some fields may be empty
        if (time.getYear().isEmpty() || this.getYear().isEmpty()) {
            //empty years are not acceptable
            return false;
        }
        if (time.getMonth().isEmpty() || this.getMonth().isEmpty()) {
            // empty month is acceptable given a matching year, underspecified date or period
            // at this point we ignore the day???
            return true;
        }

        if (!time.getMonth().equals(this.getMonth())) {
            return false;
        }

        if (time.getDay().isEmpty() || this.getDay().isEmpty()) {
            // empty day is acceptable given matching year and month
            return true;
        }

        if (!time.getDay().equals(this.getDay())) {
            return false;
        }
      //  System.out.println("this.getDateStringURI() = " + this.getDateStringURI());
      //  System.out.println();
        return true;  //// all fields match and are non-empty

    }

    public int parseTimeExValue (String timeExValue) {
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
                try {
                    if (!fields[2].equalsIgnoreCase("xx")) {
                        if (fields[2].length()>2) {
                             this.day = (new Integer(fields[2].substring(0, 2))).toString();
                        }
                        else {
                            this.day = (new Integer(fields[2])).toString();
                        }
                    }
                } catch (NumberFormatException e) {
                 //   e.printStackTrace();
                }
                try {
                    if (!fields[1].equalsIgnoreCase("xx")) this.month = (new Integer(fields[1])).toString();
                } catch (NumberFormatException e) {
                   // e.printStackTrace();
                }
                try {
                    if (!fields[0].equalsIgnoreCase("xxxx")) this.year = (new Integer(fields[0])).toString();
                } catch (NumberFormatException e) {
                  //  e.printStackTrace();
                }
            } else if (fields[2].length() == 4) {
                try {
                    if (!fields[0].equalsIgnoreCase("xx")) this.day = (new Integer(fields[0])).toString();
                } catch (NumberFormatException e) {
                 //   e.printStackTrace();
                }
                try {
                    if (!fields[1].equalsIgnoreCase("xx")) this.month = (new Integer(fields[1])).toString();
                } catch (NumberFormatException e) {
                  //  e.printStackTrace();
                }
                try {
                    if (!fields[2].equalsIgnoreCase("xxxx"))this.year = (new Integer(fields[2])).toString();
                } catch (NumberFormatException e) {
                 //   e.printStackTrace();
                }
            }
            foundTime = 1;
        }
        else if (fields.length==2) {
            this.instance = timeExValue;
            if (fields[0].length() == 4) {
                try {
                    if (fields[1].toLowerCase().startsWith("q")) {
                    ///quarters Q1, Q2, Q3, Q4 e.g. <timex3 id="tmx2" type="DATE" value="1988-Q4">
                        this.month =  fields[1];
                    }
                    if (!fields[1].equalsIgnoreCase("xx")) this.month = (new Integer(fields[1])).toString();
                } catch (NumberFormatException e) {
                    // e.printStackTrace();
                }
                try {
                    if (!fields[0].equalsIgnoreCase("xxxx")) this.year = (new Integer(fields[0])).toString();
                } catch (NumberFormatException e) {
                    //  e.printStackTrace();
                }
            }
            foundTime = 1;
        }
        else if (fields.length==1) {
            /// only the year is given
            this.instance = timeExValue;
            try {
                this.year = (new Integer(fields[0])).toString();
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            }
            foundTime = 1;
        }
        if (this.getDateLabel().isEmpty()) {
            foundTime = -1;
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
          //  System.out.println("rawdate = " + rawdate);
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


    public String getDateStringURI() {
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

    public String getDateLabel () {
        String str = this.year;
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

        Resource resource = model.createResource(this.getDateStringURI());
        Property property = model.createProperty(ResourcesUri.owltime+"DateTimeDescription");

        resource.addProperty(RDF.type, property);

        if (!this.day.isEmpty()) {
            Property day = model.createProperty(ResourcesUri.owltime+"day");
            resource.addProperty(day, this.getDayValue(), XSDDatatype.XSDgDay);
        }
        if (!this.month.isEmpty()) {
            Property month = model.createProperty(ResourcesUri.owltime+"month");
            resource.addProperty(month, this.getMonthValue(),XSDDatatype.XSDgMonth);
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

        Resource resource = model.createResource(this.getDateStringURI());
        Property property = model.createProperty(ResourcesUri.owltime+"DurationDescription");

        resource.addProperty(RDF.type, property);

        if (!this.day.isEmpty()) {
            Property day = model.createProperty(ResourcesUri.owltime+"day");
            resource.addProperty(day, this.getDayValue(), XSDDatatype.XSDgDay);
        }
        if (!this.month.isEmpty()) {
            Property month = model.createProperty(ResourcesUri.owltime+"month");
            resource.addProperty(month, this.getMonthValue(),XSDDatatype.XSDgMonth);
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
        String str = this.year;
        if (!this.month.isEmpty()) {
            str += "-";
            if (this.month.length()==1) {
                str += "0";
            }
            str += this.month;
            if (!this.day.isEmpty()) {
                str += "-";
                if (this.day.length() == 1) {
                    str += "0";
                }
                str += this.day;
            }
        }
        return str.trim();
    }

    public String toString (String granularity) {
        if (granularity.isEmpty()) {
            return toString();
        }
        String str = this.year;
        if (!granularity.equalsIgnoreCase("year")) {
            if (!this.month.isEmpty()) {
                str += "-";
                if (this.month.length() == 1) {
                    str += "0";
                }
                str += this.month;
                if (!granularity.equalsIgnoreCase("month")) {
                    if (!this.day.isEmpty()) {
                        str += "-";
                        if (this.day.length() == 1) {
                            str += "0";
                        }
                        str += this.day;
                    }
                }
            }
        }
        return str.trim();
    }

}
