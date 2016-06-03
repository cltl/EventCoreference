package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 15/04/16.
 */
public class TreeStaticHtml {

    static public final String makeHeader(String title) {
        String header = header1+title+header2;
        return header;
    }
    static public final String header1 = "\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
            "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
            "<head>\n" +
            "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
            "<title>";
    static public final String header2 = "</title>\n" +
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
            "\t\t}\n" +
            "  #row  {\n" +
            "    display: table-row;\n" +
            "    }\n" +
            "\t#left {\n" +
            "\t\twidth:150px;\n" +
            "\t\tpadding:1em;\n" +
            "\t\tbackground:#F3E2A9;\n" +
            "\t\tdisplay: table-cell;\n" +
            "\t\t}\n" +
            "\t#right {\n" +
            "\t\twidth:150px;\n" +
            "\t\tpadding:1em;\n" +
            "\t\tbackground:#F3E2A9;\n" +
            "    display: table-cell;\n" +
            "\t\t}\n" +
            "\t#cell {\n" +

            "\t\tfont-family: verdana,arial,sans-serif;\n" +
            "\t    font-size:11px;\n" +
            "\t\twidth:30px;\n" +
            "\t\tbackground:#F7F2E0;\n" +
            "\t\tborder: 0.5px solid red;\n" +
            "\t\t\n" +
            //"\t\t\tpadding: 1em;\n" +
            "\tbackground: #F7F2E0;\n" +
            "\tbackground: url(data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiA/Pgo8c3ZnIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgdmlld0JveD0iMCAwIDEgMSIgcHJlc2VydmVBc3BlY3RSYXRpbz0ibm9uZSI+CiAgPGxpbmVhckdyYWRpZW50IGlkPSJncmFkLXVjZ2ctZ2VuZXJhdGVkIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9IjAlIiB5MT0iMCUiIHgyPSIwJSIgeTI9IjEwMCUiPgogICAgPHN0b3Agb2Zmc2V0PSIwJSIgc3RvcC1jb2xvcj0iI2ViZWNkYSIgc3RvcC1vcGFjaXR5PSIxIi8+CiAgICA8c3RvcCBvZmZzZXQ9IjQwJSIgc3RvcC1jb2xvcj0iI2UwZTBjNiIgc3RvcC1vcGFjaXR5PSIxIi8+CiAgICA8c3RvcCBvZmZzZXQ9IjEwMCUiIHN0b3AtY29sb3I9IiNjZWNlYjciIHN0b3Atb3BhY2l0eT0iMSIvPgogIDwvbGluZWFyR3JhZGllbnQ+CiAgPHJlY3QgeD0iMCIgeT0iMCIgd2lkdGg9IjEiIGhlaWdodD0iMSIgZmlsbD0idXJsKCNncmFkLXVjZ2ctZ2VuZXJhdGVkKSIgLz4KPC9zdmc+);\n" +
            "\tbackground: -moz-linear-gradient(top,  #F7F2E0 0%, #F7F2E0 40%, #ceceb7 100%);\n" +
            "\tbackground: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#F7F2E0), color-stop(40%,#F7F2E0), color-stop(100%,#ceceb7));\n" +
            "\tbackground: -webkit-linear-gradient(top,  #F7F2E0 0%,#F7F2E0 40%,#ceceb7 100%);\n" +
            "\tbackground: -o-linear-gradient(top,  #F7F2E0 0%,#F7F2E0 40%,#ceceb7 100%);\n" +
            "\tbackground: -ms-linear-gradient(top,  #F7F2E0 0%,#F7F2E0 40%,#ceceb7 100%);\n" +
            "\tbackground: linear-gradient(to bottom,  #F7F2E0 0%,#F7F2E0 40%,#ceceb7 100%);\n" +
            "\tborder: 1px solid #F78181;\n" +
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
            "            \t<p>Ontology coverage</p> \n" +
            "            \t</div>";

    static public final String bodyEnd = "</body>\n" +  "</html>";
}
