package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 5/11/14.
 */
public class TimeLanguage {

    static public String [] MONTHS = null;
    static public String [] MONTHSSHORT = null;
    static public String [] DAYS = null;
    static public String [] DAYSSHORT = null;

    static final public String[] ENMONTHSSHORT = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep",
            "oct", "nov", "dec"};
    static final public String[] ENMONTHS = {"january", "february", "march", "april", "may", "june", "july",
            "augustus","september", "october", "november", "december"};
    static final public String[] MONTHSINTFULL = {"01", "02", "03", "04", "05", "06", "07", "08", "09"};
    static final public String[] MONTHSINT =  {"1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12"};

    static final public String[] ENDAYS = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
    static final public String[] ENDAYSSHORT = {"mon", "tue", "wen", "thu", "fri", "sat", "sun"};

    static final public String[] ESMONTHSSHORT = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep",
            "oct", "nov", "dic"};
    static final public String[] ESMONTHS = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto","septiembre", "octubre", "noviembre", "diciembre"};

    static final public String[] ESDAYS = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};
    static final public String[] ESDAYSSHORT = {"lun", "mar", "mie", "jue", "vie", "sab", "dom"};

    static final public String[] DAYSINT = {"1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "26", "27", "28","29", "30", "31"};

    static final public String[] DAYSINTFULL = {
            "01", "02", "03", "04", "05", "06", "07", "08", "09"};

    static public void setLanguage (String lg) {
        if (lg.equalsIgnoreCase("en")) {
            MONTHS = ENMONTHS;
            MONTHSSHORT = ENMONTHSSHORT;
            DAYS = ENDAYS;
            DAYSSHORT = ENDAYSSHORT;
        }
        else if (lg.equalsIgnoreCase("es")) {
            MONTHS = ESMONTHS;
            MONTHSSHORT = ESMONTHSSHORT;
            DAYS = ESDAYS;
            DAYSSHORT = ESDAYSSHORT;
        }
    }

    static public int getMonth (String m) {
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

    static public int getMonthWord (String m) {
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
        return -1;
    }

    static public int getDay (String day) {
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

    static public int getDayWord (String day) {
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
        return -1;
    }

    static public String getYearFromString (String date) {
        String[] words = date.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length()==4) {
                try {
                    Integer number = Integer.parseInt(word);
                    if (number>1400 && number<3000) {
                        return word;
                    }
                } catch (NumberFormatException e) {
                    // e.printStackTrace();
                }
            }
        }
        return "";
    }

    static public int getMonthFromString (String date) {
        String[] words = date.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int month = TimeLanguage.getMonth(word);
            if (month>0) {
                return month;
            }
        }
        return -1;
    }

    static public int getMonthWordFromString (String date) {
        String[] words = date.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int month = TimeLanguage.getMonthWord(word);
            if (month>0) {
                return month;
            }
        }
        return -1;
    }

    static public int getDayFromString (String date) {
        String[] words = date.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int day = TimeLanguage.getDay(word);
            if (day>0) {
                return day;
            }
        }
        return -1;
    }

    static public int getDayWordFromString (String date) {
        String[] words = date.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int day = TimeLanguage.getDayWord(word);
            if (day>0) {
                return day;
            }
        }
        return -1;
    }


}
