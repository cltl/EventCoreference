package eu.newsreader.eventcoreference.util;

/**
 * Created by piek on 15/04/16.
 */
@Deprecated
public class TreeStaticHtml {
    static final String scripts =
            "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js\"></script>\n" +
            "<script src=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script>\n" +
            "\n" +
            "<script type=\"text/javascript\">\n" +
            "    function clearValues()\n" +
            "    {\n" +
            "        var elem = document.getElementById('queryform').elements;\n" +
            "        for(var i = 0; i < elem.length; i++)\n" +
            "        {\n" +
            "            if (elem[i].checked) elem[i].checked=false;\n" +
            "           \n" +
            "        }\n" +
            "    }\n" +
            "</script>\n" +
            "\n" +
            "<script language=\"javascript\"> \n" +
            "function toggle() {\n" +
            "\tvar ele = document.getElementById(\"cell\");\n" +
            "\tvar text = document.getElementById(\"displayText\");\n" +
            "\tif(ele.style.display == \"block\") {\n" +
            "    \tele.style.display = \"none\";\n" +
            "\t\ttext.innerHTML = \"show\";\n" +
            "  \t}\n" +
            "\telse {\n" +
            "\t\tele.style.display = \"block\";\n" +
            "\t\ttext.innerHTML = \"hide\";\n" +
            "\t}\n" +
            "} \n" +
            "</script>\n" +
            "\n" +
            "<script type=\"text/javascript\">\n" +
            "            function showDiv(message) {\n" +
            "                if(document.getElementById('queryform').style.display == 'block'){\n" +
            "                    document.getElementById('queryform').style.display = \"none\";\n" +
            "                }\n" +
            "                else{\n" +
            "                    document.getElementById('queryform').style.display = \"block\";\n" +
            "                }\n" +
            "            }\n" +
            "</script>" +
            "    <script type=\"text/javascript\">\n" +
                    "\n" +
                    "    var accordionItems = new Array();\n" +
                    "\n" +
                    "    function init() {\n" +
                    "      console.log('init has been loaded')\n" +
                    "      // Grab the accordion items from the page\n" +
                    "      var divs = document.getElementsByTagName( 'div' );\n" +
                    "      for ( var i = 0; i < divs.length; i++ ) {\n" +
                    "        if ( divs[i].className == 'accordionItem' ) accordionItems.push( divs[i] );\n" +
                    "      }\n" +
                    "\n" +
                    "      // Assign onclick events to the accordion item headings\n" +
                    "      for ( var i = 0; i < accordionItems.length; i++ ) {\n" +
                    "        console.log('assigning onclick events to headings')\n" +
                    "        console.log(accordionItems[i]);\n" +
                    "\n" +
                    "        var h2 = getFirstChildWithTagName( accordionItems[i], 'H2' );\n" +
                    "        h2.onclick = toggleItem;\n" +
                    "      }\n" +
                    "\n" +
                    "      // Hide all accordion item bodies except the first\n" +
                    "      for ( var i = 1; i < accordionItems.length; i++ ) {\n" +
                    "        accordionItems[i].className = 'accordionItem hide';\n" +
                    "      }\n" +
                    "    }\n" +
                    "\n" +
                    "    function toggleItem() {\n" +
                    "\t\tconsole.log('bladerdeeg')    \n" +
                    "      var itemClass = this.parentNode.className;\n" +
                    "\n" +
                    "      // Hide all items\n" +
                    "      for ( var i = 0; i < accordionItems.length; i++ ) {\n" +
                    "        accordionItems[i].className = 'accordionItem hide';\n" +
                    "      }\n" +
                    "\n" +
                    "      // Show this item if it was previously hidden\n" +
                    "      if ( itemClass == 'accordionItem hide' ) {\n" +
                    "        this.parentNode.className = 'accordionItem';\n" +
                    "      }\n" +
                    "    }\n" +
                    "\n" +
                    "    function getFirstChildWithTagName( element, tagName ) {\n" +
                    "      console.log('getFirstChildWithTagName is called')\n" +
                    "      for ( var i = 0; i < element.childNodes.length; i++ ) {\n" +
                    "        if ( element.childNodes[i].nodeName == tagName ) return element.childNodes[i];\n" +
                    "      }\n" +
                    "    }\n" +
                    "    </script>";

    static public final String makeHeader(String title, String scripts) {
        String header = makeHeaderWithScripts(scripts)+title+"</title>\n" +
                "\" +"+header1;
        return header;
    }

    static public final String makeHeader(String title) {
        String header = makeHeaderWithoutScripts()+title+"</title>\n" +
                        scripts+header1;
        return header;
    }

    static public final String makeScripts () {
        String str =
                "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js\"></script>\n" +
                "<script src=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script>\n" +
                "<script type=\"text/javascript\">\n" +
                "    function getValues()\n" +
                "    {\n" +
                "        var str = '';\n" +
                "        var elem = document.getElementById('queryform').elements;\n" +
                "        for(var i = 0; i < elem.length; i++)\n" +
                "        {\n" +
                "            if (elem[i].checked) str += elem[i].value+\";\";\n" +
                "           \n" +
                "        }\n" +
                "        alert(str); \n" +
                "    }\n" +
                "</script>" +
                "<script type=\"text/javascript\">\n" +
                "    function clearValues()\n" +
                "    {\n" +
                "        var elem = document.getElementById('queryform').elements;\n" +
                "        for(var i = 0; i < elem.length; i++)\n" +
                "        {\n" +
                "            if (elem[i].checked) elem[i].checked=false;\n" +
                "           \n" +
                "        }\n" +
                "    }\n" +
                "</script>";
        return str;
    }

    static public final String makeScripts (int n, int m) {

        String str = "<SCRIPT LANGUAGE=\"JavaScript\">\n" +
                "function searchButton (form){\n" +
                "\tQuery = \"--event-type \";\n" +
                "    N=" + n + ";\n" +
                "\tfor (Count = 0; Count < N; Count++) {\n" +
                "        if (form.checktype[Count].checked) {\n" +
                "        \tQuery = Query + form.checktype[Count].value+\";\";\n" +
                "        }\n" +
                "    }\n" +
                "    M=" + m + ";\n" +
                "    Query = Query + \" --event-word \";\n" +
                "    for (Count = 0; Count < M; Count++) {\n" +
                "        if (form.checkword[Count].checked) {\n" +
                "        \tQuery = Query + form.checkword[Count].value+\";\";\n" +
                "        }\n" +
                "    }\n" +
                "    alert (\"I will search for: \"+Query);\n" +
                "}\n" +
                "</SCRIPT>\n" +
                "\n" +
                "<SCRIPT LANGUAGE=\"JavaScript\">\n" +
                "function clearButton (form){\n" +
                "    N=" + n + ";\n" +
                "\tfor (Count = 0; Count < N; Count++) {\n" +
                "        form.checktype[Count].checked = false;\n" +
                "    }\n" +
                "    M=" + m + ";\n" +
                "    for (Count = 0; Count < M; Count++) {\n" +
                "        form.checkword[Count].checked = false;\n" +
                "    }\n" +
                "}\n" +
                "</SCRIPT>";
        return str;
    }

    static public final String makeHeaderWithScripts (String scripts) {
        String str = "\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
                "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                "<head>\n" + scripts+"\n"+
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
                "<title>";
        return str;
    }

    static public final String makeHeaderWithoutScripts () {
        String str = "\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
                "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
                "<title>";
        return str;
    }

    static public final String header1 =
            " \n" +
            "<style>\n" +
            "ul.tab {\n" +
            "    list-style-type: none;\n" +
            "    margin: 0;\n" +
            "    padding: 0;\n" +
            "    overflow: hidden;\n" +
            "    border: 1px solid #ccc;\n" +
            "    background-color: #f1f1f1;\n" +
            "}\n" +
            "\n" +
            "/* Float the list items side by side */\n" +
            "ul.tab li {float: left;}\n" +
            "\n" +
            "/* Style the links inside the list items */\n" +
            "ul.tab li a {\n" +
            "    display: inline-block;\n" +
            "    color: black;\n" +
            "    text-align: center;\n" +
            "    padding: 14px 16px;\n" +
            "    text-decoration: none;\n" +
            "    transition: 0.3s;\n" +
            "    font-size: 17px;\n" +
            "}\n" +
            "\n" +
            "/* Change background color of links on hover */\n" +
            "ul.tab li a:hover {\n" +
            "    background-color: #ddd;\n" +
            "}\n" +
            "\n" +
            "/* Create an active/current tablink class */\n" +
            "ul.tab li a:focus, .active {\n" +
            "    background-color: #ccc;\n" +
            "}\n" +
            "\n" +
            "/* Style the tab content */\n" +
            ".tabcontent {\n" +
            "    display: none;\n" +
            "    padding: 6px 12px;\n" +
            "    border: 1px solid #ccc;\n" +
            "    border-top: none;\n" +
            "}\n" +
            "\n" +
            ".tabcontent {\n" +
            "    -webkit-animation: fadeEffect 3s;\n" +
            "    animation: fadeEffect 3s; /* Fading effect takes 1 second */\n" +
            "}\n" +
            "\n" +
            "@-webkit-keyframes fadeEffect {\n" +
            "    from {opacity: 0;}\n" +
            "    to {opacity: 1;}\n" +
            "}\n" +
            "\n" +
            "@keyframes fadeEffect {\n" +
            "    from {opacity: 0;}\n" +
            "    to {opacity: 1;}\n" +
            "}\n" +
            "\n" +
            "#container {\n" +
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

           "    body { font-size: 80%; font-family: 'Lucida Grande', Verdana, Arial, Sans-Serif;}\n" +
                    "    .accordionItem h2 { margin: 0; font-size: 1.0em; padding: 0.2em; color: #fff; background-color:#454545; }\n" +
                    "    .accordionItem h2:hover { cursor: pointer; }\n" +
                    "    .accordionItem div { margin: 0; padding: 0.2em; background-color: #FDFEFE; }\n" +
                    "    .accordionItem.hide h2 { color: #FDFEFE; background-color: #FDFEFE; }\n" +
                    "    .accordionItem.hide div { display: none; }\n" +
                    "    .accordionItem.hide #cell2 {display:none;}\n" +
                    "    \n" +
                    "    \n" +
                    "\t#container {\n" +
                    "\t  \twidth: 80%;\n" +
                    "\t  \tmargin: auto;\n" +
                    "\t\tpadding:1em;\n" +
                    "\t\tdisplay: inline;\n" +
                    "\t\t/*display: table;*/\n" +
                    "\t\tborder: none;\n" +
                    "\t}\n" +
                    "  \t#row  {\n" +
                    "    \tdisplay: table-row;\n" +
                    "    \tborder: none;\n" +
                    "\n" +
                    "   \t\t/* box-shadow: 10px 10px 5px #888888;*/\n" +
                    "    }\n" +
                    "\t#left {\n" +
                    "\t\twidth:150px;\n" +
                    "\t\tpadding:1em;\n" +
                    "\t\tbackground:#F3E2A9;\n" +
                    "\t\tdisplay: none;\n" +
                    "\t\t}\n" +
                    "\t#right {\n" +
                    "\t\twidth:250px;\n" +
                    "\t\tpadding:1em;\n" +
                    "\t\tbackground:#F3E2A9;\n" +
                    "    display: none;\n" +
                    "\t\t}\n" +
                    "\t#cell {\n" +
                    "\t\tfont-family: verdana,arial,sans-serif;\n" +
                    "\t\tbox-shadow: 2px 2px 2px #888888;\n" +
                    "\t    font-size:12px;\n" +
                    "\t    color:#944; \n" +
                    "\t\twidth:100px;\n" +
                    "\t/*    border: 1px solid #F78181; */\n" +
                    "    \tdisplay: table-cell;\n" +
                    "\t\t}\n" +
                    "\t\t\n" +
                    "\t#cell1 {\n" +
                    "\t\tfont-family: verdana,arial,sans-serif;\n" +
                    "\t    font-size:12px;\n" +
                    "\t\twidth:100px;\n" +
                    "\t\tcolor:#944; \n" +
                    "\n" +
                    "\t   /*  border: 2px solid #944; */\n" +
                    "    \tdisplay: table-cell;\n" +
                    "\t\t}\n" +
                    "\t#cell2 {\n" +
                    "        border-radius:5px 5px 5px 5px;\n" +
                    "       /* box-shadow: 10px 10px 5px #888888;*/\n" +
                    "\t\tfont-family: verdana,arial,sans-serif;\n" +
                    "\t    font-size:13px;\n" +
                    "\t    min-width:1000px;\n" +
                    "\t\tmax-width:1000px;\n" +
                    "\t    border: 1px solid #944;\n" +
                    "    \tdisplay: table-cell;\n" +
                    "\t\t}\n" +
                    "\t\t\n" +
                    "\t#cell3 {\n" +
                    "        box-shadow: 5px 5px 1px #888888;\n" +
                    "\t\tfont-family: verdana,arial,sans-serif;\n" +
                    "\t\tpadding:0.3em;\n" +
                    "\t    font-size:13px;\n" +
                    "\t    min-width:300px;\n" +
                    "\t\tmax-width:300px;\n" +
                    "\t    border: 2px solid #944;\n" +
                    "    \tdisplay: table-cell;\n" +
                    "\t\t}\n" +
                    "\t\t\t\t\n" +
                    "\t#cell4 {\n" +
                    "\t\tfont-family: verdana,arial,sans-serif;\n" +
                    "\t\tbox-shadow: 2px 2px 2px #888888;\n" +
                    "\t    font-size:12px;\n" +
                    "\t    color:#944; \n" +
                    "\t\twidth:250px;\n" +
                    "\t/*    border: 1px solid #F78181; */\n" +
                    "    \tdisplay: table-cell;\n" +
                    "\t\t}\n" +
                    "\t\t\n" +
                    "\t#cell5 {\n" +
                    "        box-shadow: 5px 5px 1px #888888;\n" +
                    "\t\tfont-family: verdana,arial,sans-serif;\n" +
                    "\t\tpadding:0.3em;\n" +
                    "\t    font-size:13px;\n" +
                    "\t    min-width:200px;\n" +
                    "\t\tmax-width:200px;\n" +
                    "\t    border: 2px solid #944;\n" +
                    "\t\tdisplay: table-cell;\n" +
                    "\t\t}\n" +
                    "\t\t\n" +
                    "\t\t#cell6 {\n" +
                    "\t\tfont-family: verdana,arial,sans-serif;\n" +
                    "\t\tpadding:0.3em;\n" +
                    "\t\tfont-size:13px;\n" +
                    "\t\tmin-width:800px;\n" +
                    "\t\tmax-width:800px;\n" +
                    "\t\tdisplay: table-cell;"+

                    "\t\t}"+
                    "\t\t\t\n" +
                    "\t#cell7 {\n" +
                    "\t\tfont-family: verdana,arial,sans-serif;\n" +
                    "\t\tbox-shadow: 2px 2px 2px #888888;\n" +
                    "\t    font-size:12px;\n" +
                    "\t    color:#944; \n" +
                    "\t\twidth:100px;\n" +
                    "\t/*    border: 1px solid #F78181; */\n" +
                    "    \tdisplay: table-cell;\n" +
                    "\t\t}"+

            "</style>"+
            "\n" +
            "\n" +
            "</head>\n" ;


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
            "#cell2 {\n" +
            "\t\tfont-family: verdana,arial,sans-serif;\n" +
            "\t    font-size:11px;\n" +
            "\t\twidth:30px;\n" +
            "\t   /*  border: 2px solid #F78181; */\n" +
            "    \tdisplay: table-cell;\n" +
            "\t\t}\n"+
            "\t#cell {\n" +

            "\t\tfont-family: verdana,arial,sans-serif;\n" +
            "\t    font-size:11px;\n" +
            "\t\twidth:30px;\n" +
            //"\t\tbackground:#F7F2E0;\n" +
            "\t\tborder: 0.5px solid red;\n" +
            "\t\t\n" +
            //"\t\t\tpadding: 1em;\n" +
            /*"\tbackground: #F7F2E0;\n" +
            "\tbackground: url(data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiA/Pgo8c3ZnIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgdmlld0JveD0iMCAwIDEgMSIgcHJlc2VydmVBc3BlY3RSYXRpbz0ibm9uZSI+CiAgPGxpbmVhckdyYWRpZW50IGlkPSJncmFkLXVjZ2ctZ2VuZXJhdGVkIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9IjAlIiB5MT0iMCUiIHgyPSIwJSIgeTI9IjEwMCUiPgogICAgPHN0b3Agb2Zmc2V0PSIwJSIgc3RvcC1jb2xvcj0iI2ViZWNkYSIgc3RvcC1vcGFjaXR5PSIxIi8+CiAgICA8c3RvcCBvZmZzZXQ9IjQwJSIgc3RvcC1jb2xvcj0iI2UwZTBjNiIgc3RvcC1vcGFjaXR5PSIxIi8+CiAgICA8c3RvcCBvZmZzZXQ9IjEwMCUiIHN0b3AtY29sb3I9IiNjZWNlYjciIHN0b3Atb3BhY2l0eT0iMSIvPgogIDwvbGluZWFyR3JhZGllbnQ+CiAgPHJlY3QgeD0iMCIgeT0iMCIgd2lkdGg9IjEiIGhlaWdodD0iMSIgZmlsbD0idXJsKCNncmFkLXVjZ2ctZ2VuZXJhdGVkKSIgLz4KPC9zdmc+);\n" +
            "\tbackground: -moz-linear-gradient(top,  #F7F2E0 0%, #F7F2E0 40%, #ceceb7 100%);\n" +
            "\tbackground: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#F7F2E0), color-stop(40%,#F7F2E0), color-stop(100%,#ceceb7));\n" +
            "\tbackground: -webkit-linear-gradient(top,  #F7F2E0 0%,#F7F2E0 40%,#ceceb7 100%);\n" +
            "\tbackground: -o-linear-gradient(top,  #F7F2E0 0%,#F7F2E0 40%,#ceceb7 100%);\n" +
            "\tbackground: -ms-linear-gradient(top,  #F7F2E0 0%,#F7F2E0 40%,#ceceb7 100%);\n" +
            "\tbackground: linear-gradient(to bottom,  #F7F2E0 0%,#F7F2E0 40%,#ceceb7 100%);\n" +*/
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

    static public final String bodyStartOld =
            "            <body> \n" +
            "            \t<div id=\"header\"> \n" +
            "            \t<p>Ontology coverage</p> \n" +
            "            \t</div>";

    static public String makeBodyStart(String title, String path, int nDocs, int nEvents, int nEntities, int nSources) {
        String bodyStart = "<body onload=\"init()\">\n" +
                "\n" +
                "<p>NewsReader Storyteller</p>\n" +
                "<h1>"+title+"</h1>\n" +
                "<div id=\"container\">\n" +
                "<div id=\"row\"><div id=\"cell\">Nr. of documents</div><div id=\"cell\">904</div></div>\n" +
                "<div id=\"row\"><div id=\"cell\">Nr. of events</div><div id=\"cell\">78,531</div></div>\n" +
                "<div id=\"row\"><div id=\"cell\">Nr. of entities</div><div id=\"cell\"></div></div>\n" +
                "<div id=\"row\"><div id=\"cell\">Nr. of sources</div><div id=\"cell\">876</div></div>\n" +
                "</div>"+
                "<hr>\n" +
                "<p>\n" +
                "<div>\n" +
                "\n" +
                "{% if finished %}\n" +
                "<b>Result</b>: <font size=0.5>{{query}}</font>\n" +
                "{% endif %}\n" +
                "</div>\n" +
                "<hr>"+
                "\n" +
                "<FORM ID=\"queryform\" NAME=\"queryform\" action='/"+path+"' method=\"POST\"> \n" +
                "<input type='submit' value='Search'/>\n" +
                "<input TYPE=\"button\" NAME=\"clear\" Value=\"Clear\" onClick=\"clearValues()\"/>\n"+
                "\n" +
                "<ul class=\"tab\">\n" +
                "  <li><a href=\"#\" class=\"tablinks\" onclick=\"openData(event, 'Entities')\">Entities</a></li>\n" +
                "  <li><a href=\"#\" class=\"tablinks\" onclick=\"openData(event, 'Events')\">Events</a></li>\n" +
                "  <li><a href=\"#\" class=\"tablinks\" onclick=\"openData(event, 'Sources')\">Sources</a></li>\n" +
                "  <li><a href=\"#\" class=\"tablinks\" onclick=\"openData(event, 'Perspectives')\">Perspectives</a></li>\n" +
                "  <li><a href=\"#\" class=\"tablinks\" onclick=\"openData(event, 'Topics')\">Topics</a></li>\n" +
                "</ul>";
        return bodyStart;
    }

    static public final String bodyEndOld = "</body>\n" +  "</html>";

    static public final String bodyEnd =
            "<script>\n" +
            "function openData(evt, aName) {\n" +
            "    var i, tabcontent, tablinks;\n" +
            "    tabcontent = document.getElementsByClassName(\"tabcontent\");\n" +
            "    for (i = 0; i < tabcontent.length; i++) {\n" +
            "        tabcontent[i].style.display = \"none\";\n" +
            "    }\n" +
            "    tablinks = document.getElementsByClassName(\"tablinks\");\n" +
            "    for (i = 0; i < tablinks.length; i++) {\n" +
            "        tablinks[i].className = tablinks[i].className.replace(\" active\", \"\");\n" +
            "    }\n" +
            "    document.getElementById(aName).style.display = \"block\";\n" +
            "    evt.currentTarget.className += \" active\";\n" +
            "}\n" +
            "</script>\n" +
            "     \n" +
            "</body>\n" +
            "</html>";


    static public final String formStartJS = "<FORM ID=\"queryform\" NAME=\"queryform\">  \n" +
            "<INPUT TYPE=\"button\" NAME=\"search\" Value=\"Search\" onClick=\"getValues()\"><BR>\n" +
            "<INPUT TYPE=\"button\" NAME=\"clear\" Value=\"Clear\" onClick=\"clearValues()\"><BR>  ";

    static public final String formEnd = "</FORM>";

    static public String makeTickBox (String type, String name) {
        String tb = "<INPUT TYPE=\"checkbox\" NAME=\""+type+"\" VALUE=\""+name+"\">";
        return tb;
    }

    static public String makeTickBox (String type, String name, String value) {
        String tb = "<INPUT TYPE=\"checkbox\" NAME=\""+type+"\" VALUE=\""+value+"\">";
        return tb;
    }

    static final String toggleJavaScript = "    <script type=\"text/javascript\">\n" +
            "\n" +
            "    var accordionItems = new Array();\n" +
            "\n" +
            "    function init() {\n" +
            "      console.log('init has been loaded')\n" +
            "      // Grab the accordion items from the page\n" +
            "      var divs = document.getElementsByTagName( 'div' );\n" +
            "      for ( var i = 0; i < divs.length; i++ ) {\n" +
            "        if ( divs[i].className == 'accordionItem' ) accordionItems.push( divs[i] );\n" +
            "      }\n" +
            "\n" +
            "      // Assign onclick events to the accordion item headings\n" +
            "      for ( var i = 0; i < accordionItems.length; i++ ) {\n" +
            "        console.log('assigning onclick events to headings')\n" +
            "        console.log(accordionItems[i]);\n" +
            "\n" +
            "        var h2 = getFirstChildWithTagName( accordionItems[i], 'H2' );\n" +
            "        h2.onclick = toggleItem;\n" +
            "      }\n" +
            "\n" +
            "      // Hide all accordion item bodies except the first\n" +
            "      for ( var i = 1; i < accordionItems.length; i++ ) {\n" +
            "        accordionItems[i].className = 'accordionItem hide';\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    function toggleItem() {\n" +
            "\t\tconsole.log('bladerdeeg')    \n" +
            "      var itemClass = this.parentNode.className;\n" +
            "\n" +
            "      // Hide all items\n" +
            "      for ( var i = 0; i < accordionItems.length; i++ ) {\n" +
            "        accordionItems[i].className = 'accordionItem hide';\n" +
            "      }\n" +
            "\n" +
            "      // Show this item if it was previously hidden\n" +
            "      if ( itemClass == 'accordionItem hide' ) {\n" +
            "        this.parentNode.className = 'accordionItem';\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    function getFirstChildWithTagName( element, tagName ) {\n" +
            "      console.log('getFirstChildWithTagName is called')\n" +
            "      for ( var i = 0; i < element.childNodes.length; i++ ) {\n" +
            "        if ( element.childNodes[i].nodeName == tagName ) return element.childNodes[i];\n" +
            "      }\n" +
            "    }\n" +
            "    </script>\n";
}
