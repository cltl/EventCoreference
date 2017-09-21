package eu.newsreader.eventcoreference.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Created by piek on 04/05/2017.
 */
public class PipelineOverview {


    static public void main (String [] args) {
        String path = "";
        if (args.length==0) {
            path = ".";
        }
        else {
            path = args[0];
        }
        ArrayList<File> textFiles = Util.makeFlatFileList(new File(path), ".txt");
        ArrayList<File> innafFiles = Util.makeFlatFileList(new File(path), ".in.naf");
        ArrayList<File> nafFiles = Util.makeFlatFileList(new File(path), ".out.naf");
        ArrayList<File> trigFiles = Util.makeFlatFileList(new File(path), ".trig");
        ArrayList<File> ttlFiles = Util.makeFlatFileList(new File(path), ".ttl");
        ArrayList<File> svgFiles = Util.makeFlatFileList(new File(path), ".svg");
        try {
            OutputStream fos = new FileOutputStream(path+"/"+"index.html");
            String str = makeHtml(textFiles, innafFiles, nafFiles, trigFiles, ttlFiles, svgFiles);
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static String css = "<style>\n.divTable{\n" +
            "\tdisplay: table;\n" +
            "\twidth: 100%;\n" +
            "}\n" +
            ".divTableRow {\n" +
            "\tdisplay: table-row;\n" +
            "}\n" +
            ".divTableHeading {\n" +
            "\tbackground-color: #EEE;\n" +
            "\tdisplay: table-header-group;\n" +
            "}\n" +
            ".divTableCell, .divTableHead {\n" +
            "\tborder: 1px solid #999999;\n" +
            "\tdisplay: table-cell;\n" +
            "\tpadding: 3px 10px;\n" +
            "}\n" +
            ".divTableHeading {\n" +
            "\tbackground-color: #EEE;\n" +
            "\tdisplay: table-header-group;\n" +
            "\tfont-weight: bold;\n" +
            "}\n" +
            ".divTableFoot {\n" +
            "\tbackground-color: #EEE;\n" +
            "\tdisplay: table-footer-group;\n" +
            "\tfont-weight: bold;\n" +
            "}\n" +
            ".divTableBody {\n" +
            "\tdisplay: table-row-group;\n" +
            "}\n</style>\n";

    static String makeHtml (ArrayList<File> textFiles, ArrayList<File> innafFiles, ArrayList<File> nafFiles, ArrayList<File> trigFiles, ArrayList<File> ttlFiles, ArrayList<File> svgFiles) {
        String html =
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
                        "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                        "<head>\n" +
                        //"<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
                        "<title>NewsReader pipeline results</title>\n";
        html += css;
                 html += "</head>\n";
                html+="<body>\n" +
                        "<h1>Results of the NewsReader pipeline</h1>\n"+
                        "<a href=\"README\">How to run this demo? See README</a>\n" +
                        "<div class=\"divTable\" style=\"width: 100%;border: 1px solid #000;\">\n" +
                "<div class=\"divTableBody\">\n";
                html += "<div class=\"divTableRow\">\n" +
                        "<div class=\"divTableCell\">Text</div>\n" +
                        "<div class=\"divTableCell\">Kb size</div>\n" +
                        "<div class=\"divTableCell\">Date</div>\n" +
                        "<div class=\"divTableCell\">In NAF</a></div>\n" +
                        "<div class=\"divTableCell\">Size</div>\n" +
                        "<div class=\"divTableCell\">Date</div>\n" +
                        "<div class=\"divTableCell\">Out NAF</a></div>\n" +
                        "<div class=\"divTableCell\">Size</div>\n" +
                        "<div class=\"divTableCell\">Date</div>\n" +
                        "<div class=\"divTableCell\">TRiG</div>\n" +
                        "<div class=\"divTableCell\">Kb size</div>\n" +
                        "<div class=\"divTableCell\">Date</div>\n" +
                        "<div class=\"divTableCell\">TTL</div>\n" +
                        "<div class=\"divTableCell\">Kb size</div>\n" +
                        "<div class=\"divTableCell\">Date</div>\n" +
                        "<div class=\"divTableCell\">Graph</div>\n" +
                        "<div class=\"divTableCell\">Kb size</div>\n" +
                        "<div class=\"divTableCell\">Date</div>\n" +
                        "</div>\n";
        Collections.sort(textFiles);
        for (int i = 0; i < textFiles.size(); i++) {
            File textFile = textFiles.get(i);
            File trigFile = null;
            File innafFile = null;
            File nafFile = null;
            File ttlFile = null;
            File svgFile = null;
            for (int j = 0; j < innafFiles.size(); j++) {
                File file = innafFiles.get(j);
                if (file.getName().startsWith(textFile.getName())) {
                    innafFile = file;
                    break;
                }
            }
            for (int j = 0; j < nafFiles.size(); j++) {
                File file = nafFiles.get(j);
                if (file.getName().startsWith(textFile.getName())) {
                    nafFile = file;
                    break;
                }
            }
            for (int j = 0; j < ttlFiles.size(); j++) {
                File file = ttlFiles.get(j);
                if (file.getName().startsWith(textFile.getName())) {
                    ttlFile = file;
                    break;
                }
            }
            for (int j = 0; j < trigFiles.size(); j++) {
                File file = trigFiles.get(j);
                if (file.getName().startsWith(textFile.getName())) {
                    trigFile = file;
                    break;
                }
            }
            for (int j = 0; j < svgFiles.size(); j++) {
                File file = svgFiles.get(j);
                if (file.getName().startsWith(textFile.getName())) {
                    svgFile = file;
                    break;
                }
            }
            html += makeRow(textFile, innafFile, nafFile, trigFile, ttlFile, svgFile);
        }

        
        html += "</div>\n";
        html += "</div>\n";

        html += "<table style=\"width:100%\">\n";
        html += "<tr> \n" +
                "<td>\n" +
                "<h1>English pipeline and NAF layers</h1>\n" +
                "<img src=\"EnglishPipeline.png\" width=\"500\" height=\"500\">\n" +
                "</td>\n" +
                "<td>\n" +
                "<h1>Dutch pipeline and NAF layers</h1>\n" +
                "<img src=\"DutchPipeline.png\" width=\"500\" height=\"500\">\n" +
                "</td>\n"+
                "</tr>\n";
        html += "</table>\n";
        html += "</body>\n";
        return html;
    }

    static String makeRow (File file1, File file2, File file3, File file4, File file5, File file6) {
        String row = " <div class=\"divTableRow\">\n"
                + makeHref(file1)
                + makeHref(file2)
                + makeHref(file3)
                + makeHref(file4)
                + makeHref(file5)
                + makeHref(file6)
                +"</div>\n";
        return row;
    }

    static String makeHref (File file) {
        String href = "";
        if (file == null) {
            href ="<div class=\"divTableCell\">"+"no file"+"</div>\n" +
                    "<div class=\"divTableCell\">"+"</div>\n" +
                    "<div class=\"divTableCell\">"+"</div>\n";
        }
        else {
                String path = "http://kyoto.let.vu.nl/~vossen/nwr/pipelinedemo/"+file.getName();
                Date date = new Date(file.lastModified());
                String format = "yyyy/MM/dd/HH:mm:ss";
                Locale locale = Locale.ENGLISH;
                String formattedDateString = new SimpleDateFormat(format, locale).format(date);
                href ="<div class=\"divTableCell\">"+"<a href=\""+path+"\" target=\"_blank\">"+file.getName()+"</a>"+"</div>\n" +
                       "<div class=\"divTableCell\">"+file.length()+"</div>\n" +
                       "<div class=\"divTableCell\">"+formattedDateString+"</div>\n";

        }
        return href;
    }
}
