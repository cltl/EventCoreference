package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import eu.newsreader.eventcoreference.naf.ResourcesUri;

/**
 * Created by piek on 4/10/14.
 */
public class OwlTime {

    static final public String[] MONTHSSHORT = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep",
            "oct", "nov", "dec"};
    static final public String[] MONTHS = {"january", "february", "march", "april", "may", "june", "july",
            "augustus","september", "october", "november", "december"};
    static final public String[] MONTHSINTFULL = {"01", "02", "03", "04", "05", "06", "07", "08", "09"};
    static final public String[] MONTHSINT =  {"1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12"};

    static final public String[] DAYS = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
    static final public String[] DAYSSHORT = {"mon", "tue", "wen", "thu", "fri", "sat", "sun"};

    static final public String[] DAYSINT = {"1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "26", "27", "28","29", "30", "31"};

    static final public String[] DAYSINTFULL = {
            "01", "02", "03", "04", "05", "06", "07", "08", "09"};

    static int getMonth (String m) {
        for (int i = 0; i < MONTHS.length; i++) {
            String month = MONTHS[i];
            if (month.equalsIgnoreCase(m)) {
                return i+1;
            }
        }
        for (int i = 0; i < MONTHSSHORT.length; i++) {
            String month = MONTHSSHORT[i];
            if (month.equalsIgnoreCase(m)) {
                return i+1;
            }
        }
        for (int i = 0; i < MONTHSINT.length; i++) {
            String month = MONTHSINT[i];
            if (month.equalsIgnoreCase(m)) {
                return i+1;
            }
        }
        for (int i = 0; i < MONTHSINTFULL.length; i++) {
            String month = MONTHSINTFULL[i];
            if (month.equalsIgnoreCase(m)) {
                return i+1;
            }
        }
        return -1;
    }

    static int getDay (String day) {
        for (int i = 0; i < DAYS.length; i++) {
            String s = DAYS[i];
            if (s.equalsIgnoreCase(day)) {
                return i+1;
            }
        }
        for (int i = 0; i < DAYSSHORT.length; i++) {
            String s = DAYSSHORT[i];
            if (s.equalsIgnoreCase(day)) {
                return i+1;
            }
        }
        for (int i = 0; i < DAYSINT.length; i++) {
            String s = DAYSINT[i];
            if (s.equalsIgnoreCase(day)) {
                return i+1;
            }
        }
        for (int i = 0; i < DAYSINTFULL.length; i++) {
            String s = DAYSINTFULL[i];
            if (s.equalsIgnoreCase(day)) {
                return i+1;
            }
        }
        return -1;
    }
            //new ArrayList<String>("","");

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

    public void parsePublicationDate (String date) {
        ///2013-01-01
        String [] fields = date.split("-");
        if (fields.length==3) {
            //System.out.println("date = " + date);
            this.instance = date;
            this.day = (new Integer(fields[2])).toString();
            this.month = (new Integer(fields[1])).toString();
            this.year = (new Integer(fields[0])).toString();
        }
    }

    public void parseStringDate (String date) {
        ///2013-01-01
        String year = getYearFromString(date);
        if (!year.isEmpty()) {
            this.year = year;
            int month = getMonthFromString(date);
            if (month>-1) {
                this.month = (new Integer(month)).toString();
                int day = getDayFromString(date);
                if (day>-1) {
                    this.day = (new Integer(day).toString());
                }
            }
        }
    }

    static public String getYearFromString (String date) {
        String[] words = date.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length()==4) {
                try {
                    Integer number = Integer.parseInt(word);
                    if (number>1800 && number<2050) {
                        return word;
                    }
                } catch (NumberFormatException e) {
                   // e.printStackTrace();
                }
            }
        }
        return "";
    }

    public int getMonthFromString (String date) {
        String[] words = date.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int month = getMonth(word);
            if (month>0) {
               return month;
            }
        }
        return -1;
    }

    public int getDayFromString (String date) {
        String[] words = date.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int day = getDay(word);
            if (day>0) {
               return day;
            }
        }
        return -1;
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
        Property property = model.createProperty("time:DateTimeDescription");

        resource.addProperty(RDF.type, property);

        if (!this.day.isEmpty()) {
            Property day = model.createProperty("time:day");
            resource.addProperty(day, this.getDay(), XSDDatatype.XSDgDay);
        }
        if (!this.month.isEmpty()) {
            Property month = model.createProperty("time:month");
            resource.addProperty(month, this.getMonth(),XSDDatatype.XSDgMonth);
        }
        if (!this.year.isEmpty()) {
            Property year = model.createProperty("time:year");
            resource.addProperty(year, this.getYear(),XSDDatatype.XSDgYear);
            Property unit = model.createProperty("time:unitType");
            Property day = model.createProperty("time:unitDay");
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
        Property property = model.createProperty("time:DurationDescription");

        resource.addProperty(RDF.type, property);

        if (!this.day.isEmpty()) {
            Property day = model.createProperty("time:day");
            resource.addProperty(day, this.getDay(), XSDDatatype.XSDgDay);
        }
        if (!this.month.isEmpty()) {
            Property month = model.createProperty("time:month");
            resource.addProperty(month, this.getMonth(),XSDDatatype.XSDgMonth);
        }
        if (!this.year.isEmpty()) {
            Property year = model.createProperty("time:year");
            resource.addProperty(year, this.getYear(),XSDDatatype.XSDgYear);
            Property unit = model.createProperty("time:unitType");
            Property day = model.createProperty("time:unitDay");
            resource.addProperty(unit, day);
        }
    }

}
