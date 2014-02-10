package eu.newsreader.eventcoreference.objects;

/**
 * Created by piek on 2/5/14.
 */
public class SourceMeta {
   // http://www.newsreader-project.eu/2003/10/10/49RH-WKG0-010D-R025.xml
   // 	49RH-WKG0-010D-R025
   // 	2003-10-10
   // 	FINANCIAL NEWS
   // 	PR Newswire
   // 		SEOUL, Korea Oct. 10
   // 	Hyundai NEOS-II Crossover Concept to Debut at 2003 Tokyo Motor Show
   // 	 Copyright 2003 PR Newswire Association, Inc.
   // 	1550
    /**
     *

     * field 0 = http://www.newsreader-project.eu/2011/1/21/524R-JWB1-JCS0-V54Y.xml
     field 1 = 524R-JWB1-JCS0-V54Y
     field 2 = 2011-01-21
     field 3 = OTHER
     field 4 = Breaking News from globeandmail.com
     field 5 = JEREMY CATO
     field 6 =
     field 7 = Mercedes-Benz takes crown from BMW; Benz ends BMW s eight-year reign as king of the premium brands   now what?
     field 8 =  Copyright 2011 The Globe and Mail, a division of CTVglobemedia Publishing Inc. All Rights Reserved
     field 9 = 5729

     field 0 = http://www.newsreader-project.eu/2011/1/21/520D-CBF1-F0JP-W4T4.xml
     field 1 = 520D-CBF1-F0JP-W4T4
     field 2 = 2011-01-21
     field 3 = CARSGUIDE; Pg. 41
     field 4 = Herald Sun (Australia)
     field 5 = James Stanford
     field 6 =
     field 7 = Who makes what
     field 8 =  Copyright 2011 Nationwide News Pty Limited All Rights Reserved
     field 9 = 4232

     field 0 = http://www.newsreader-project.eu/2011/1/21/520K-Y7H1-JBKR-C3W7.xml
     field 1 = 520K-Y7H1-JBKR-C3W7
     field 2 = 2011-01-21
     field 3 = DRIVING.CA; Auto File; Pg. F4
     field 4 = The Calgary Herald (Alberta)
     field 5 = Calgary Herald
     field 6 =
     field 7 = Ford goes greener with biomaterials used in the manufacturing of vehicles
     field 8 =  Copyright 2011 The Calgary Herald, a division of Canwest MediaWorks Publication Inc. All Rights Reserved
     field 9 = 1633

     field 0 = http://www.newsreader-project.eu/2011/1/21/520K-Y9P1-JBKR-9067.xml
     field 1 = 520K-Y9P1-JBKR-9067
     field 2 = 2011-01-21
     field 3 = DRIVING; Pg. F2
     field 4 = Times Colonist (Victoria, British Columbia)
     field 5 = Bloomberg
     field 6 =
     field 7 = Lexus holds off rivals in luxury-car sales
     field 8 =  Copyright 2011 Times Colonist, a division of Canwest MediaWorks Publication Inc. All Rights Reserved
     field 9 = 4245
     */

    private String sourceId;
    private String dateString;
    private String section;
    private String author;
    private String location;
    private String title;
    private String owner;
    private String nChar;

    public SourceMeta() {
        this.sourceId = "";
        this.dateString = "";
        this.section = "";
        this.author = "";
        this.location = "";
        this.title = "";
        this.owner = "";
        this.nChar = "";
    }

    public SourceMeta(String[] fields) {
        /**
         *
         field 0 = http://www.newsreader-project.eu/2011/1/21/520D-CBF1-F0JP-W4T4.xml
         field 1 = 520D-CBF1-F0JP-W4T4
         field 2 = 2011-01-21
         field 3 = CARSGUIDE; Pg. 41
         field 4 = Herald Sun (Australia)
         field 5 = James Stanford
         field 6 =
         field 7 = Who makes what
         field 8 =  Copyright 2011 Nationwide News Pty Limited All Rights Reserved
         field 9 = 4232
         */
        this.sourceId = fields[1].trim();
        this.dateString = fields[2].trim();
        this.section = fields[3].trim();
        this.owner = fields[4].trim().replace(" ", "_");
        this.author = fields[5].trim().replace(" ", "_");
        this.location = fields[6].trim();
        this.title = fields[7].trim();
        // field[8] == copy right statement
        this.nChar = fields[9].trim();
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getnChar() {
        return nChar;
    }

    public void setnChar(String nChar) {
        this.nChar = nChar;
    }
}
