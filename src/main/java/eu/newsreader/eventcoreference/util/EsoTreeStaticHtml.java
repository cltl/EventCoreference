package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 15/04/16.
 */
public class EsoTreeStaticHtml {

    static public final String header = "\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
            "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
            "<head>\n" +
            "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
            "<title>ESO tree view</title>\n" +
            "\n" +
            "<style type=\"text/css\">\n" +
            "\n" +
            "\tbody {\n" +
            "\t\tmargin:0; padding:0;\n" +
            "\t\tfont-size:80%;\n" +
            "\t\tfont-family: sans-serif;\n" +
            "\t\t}\n" +
            "  #header {\n" +
            "    display: block;\n" +
            "    width: 80%;\n" +
            "    margin: auto;\n" +
            "    }\n" +
            "\t#container {\n" +
            "\t  width: 80%;\n" +
            "\t  margin: auto;\n" +
            "\t\tpadding:0;\n" +
            "\t\tdisplay: table;\n" +
            "\t\tborder: 1px solid black;\n" +
            "\t\t}\n" +
            "  #row  {\n" +
            "    display: table-row;\n" +
            "    }\n" +
            "\t#left {\n" +
            "\t\twidth:150px;\n" +
            "\t\tpadding:1em;\n" +
            "\t\tbackground: #EEF;\n" +
            "\t\tdisplay: table-cell;\n" +
            "\t\t}\n" +
            "\t#right {\n" +
            "\t\twidth:150px;\n" +
            "\t\tpadding:1em;\n" +
            "\t\tbackground:#FEE;\n" +
            "    display: table-cell;\n" +
            "\t\t}\n" +
            "\t#cell {\n" +
            "\t\twidth:15px;\n" +
            //"\t\tpadding: 1em;\n" +
            "\t\tbackground:#EEF;\n" +
            "    \tdisplay: table-cell;\n" +
            "\t\t}\n" +
            "\t\t\n" +
            "/* ]]> */\n" +
            "</style>\n" +
            "\n" +
            "\n" +
            "\n" +
            "</head>\n" ;

    static public final String bodyStart =
            "            <body> \n" +
            "            \t<div id=\"header\"> \n" +
            "            \t<p>Brexit</p> \n" +
            "            \t</div>";

    static public final String bodyEnd = "</body>\n" +  "</html>";
}
