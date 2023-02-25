package com.bkk.de.gluex;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class InitBean implements InitializingBean {


    Splitter splitter;
    List<String> coordinatesList;
    PDF pdf;

    public InitBean(PDF pdf) {
        this.pdf = pdf;
        this.splitter = new Splitter();
        coordinatesList = new ArrayList<>();
        coordinatesList.add("121,59,652,390");
        coordinatesList.add("121,52,652,390");
        coordinatesList.add("121,38,652,360");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean#afterPropertiesSet()");
        pdf.getAllFiles("/home/bkk/gluex/pdf")
                .forEach(f -> {
                    System.out.println(f.getFileName());
                    split(f);
                });
    }

    private void deleteTmpFile() {
        File myObj = new File("/tmp/gluex.pdf");
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
        } else {
            System.out.println("Failed to delete the file." + myObj.getName());
        }
    }

    private void split(Path documentPath) {
        try {
            deleteTmpFile();
            PDDocument document = PDDocument.load(documentPath.toFile());
            int loops = document.getNumberOfPages() < 10 ? document.getNumberOfPages() - 1 : 10;
            for (int i = loops; i > 1; i--) {
                    PDDocument page = new PDDocument();
                    page.addPage(document.getPage(i));
                    page.save(new File("/tmp/gluex.pdf"));
                    if (checkStringWettrunde(page)) {
                        page.save(new File("/tmp/gluex.pdf"));
                        runThroughCoordinatesList(documentPath.getFileName());
                        return;
                    }

            }
            System.out.println("ERROR cannot find score page!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkStringWettrunde(PDDocument page) {
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            List<String> items = Arrays.asList(pdfStripper.getText(page).split("\n"));
            for (String str : items) {
                if (str.startsWith("6aus45 Auswahlwette")) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void runThroughCoordinatesList(Path file) {
        for (String str : coordinatesList) {
            if (extractData(str)) {
                return;
            }
        }
        System.out.println("ERROR " + file);
    }

    private boolean extractData(String coordinates) {

        try {
            Process p = Runtime.getRuntime().exec("java -jar /home/bkk/bin/tabula-1.0.5-jar-with-dependencies.jar -n -p 1 -a " + coordinates + " /tmp/gluex.pdf");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String s;
            while ((s = stdInput.readLine()) != null) {
                stringBuilder.append(s);
                stringBuilder.append("\n");
            }
            String strSplit[] = stringBuilder.toString().split(",");
            if(strSplit[0].length()>2) {
                stringBuilder.replace(0,strSplit[0].length(),strSplit[0].replace(" ",","));
            }
            String strSplit2[] = stringBuilder.toString().split(",");
            if (strSplit2[0].equals("1")) {
                File file = new File("/tmp/file.txt");
                BufferedWriter writer = null;
                try {
                    corrections(stringBuilder);
                    writer = new BufferedWriter(new FileWriter(file));
                    writer.append(stringBuilder);
                    System.out.println(stringBuilder);
                } finally {
                    if (writer != null) writer.close();
                }
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void corrections(StringBuilder stringBuilder) {
        replaceAll(stringBuilder,"\"\",,,,,,,HC*,\n","");
        replaceAll(stringBuilder,"\"\",,,,,,,HC*,,\n","");
        replaceAll(stringBuilder,"\"\",,,,,,HC*,,\n","");
        replaceAll(stringBuilder,"\"\",,,,,HC*,,\n","");
        replaceAll(stringBuilder,"\"\",,,,,HC*\n","");
        replaceAll(stringBuilder,"\"\",,,,,,HC*\n","");
        replaceAll(stringBuilder,"\"\",,,,,,HC*,,,\n","");
        replaceAll(stringBuilder,"\"\",,,,,,,HC*\n","");
        replaceAll(stringBuilder,"\"\",,,,,,HC*,\n","");
        replaceAll(stringBuilder,"\"\",,,,,HC*,\n","");
        replaceAll(stringBuilder,"\"\",,,,,Ergebnis ohne HC*,,\n","");
        replaceAll(stringBuilder,"Ergebnis ohne HC*",",");
        replaceAll(stringBuilder,"Ergebnis mit HC*","");


    }
    public static void replaceAll(StringBuilder stringBuilder, String from, String to) {
        int index = stringBuilder.indexOf(from);
        if(index > 0) {
            stringBuilder.replace(index, index + from.length(), to);
        }
    }

}
