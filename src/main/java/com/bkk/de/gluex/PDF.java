package com.bkk.de.gluex;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class PDF {

    Splitter splitter;
    List<String> coordinatesList;
    Redis redis;


    @Autowired
    public PDF(Redis redis) {
        this.redis = redis;
        this.splitter = new Splitter();
        this.coordinatesList = new ArrayList<>();
    }

    public Set<Path> getAllFiles(String dir) throws IOException {
        Set<Path> fileSet = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileSet.add(path);
                }
            }
        }
        return fileSet;
    }

    private void deleteTmpFile() {
        File myObj = new File("/tmp/gluex.pdf");
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
        } else {
            System.out.println("Failed to delete the file." + myObj.getName());
        }
    }

    public void createScores(Path documentPath) {
        split(documentPath);
    }

    public void split(Path documentPath) {
        try {
            deleteTmpFile();
            PDDocument document = PDDocument.load(documentPath.toFile());
            int loops = document.getNumberOfPages() < 10 ? document.getNumberOfPages() - 1 : 10;
            for (int i = loops; i > 1; i--) {
                PDDocument page = new PDDocument();
                page.addPage(document.getPage(i));
                page.save(new File("/tmp/gluex.pdf"));
                if (checkStringAuswahlwette(page)) {
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

    private boolean checkStringAuswahlwette(PDDocument page) {
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            List<String> items = Arrays.asList(pdfStripper.getText(page).split("\n"));
            for (String str : items) {
                if (str.startsWith("6aus45 Auswahlwette") || str.startsWith("13er Ergebnistipp / 6aus45 Auswahltipp")) {
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
            if (extractData(str, file.getFileName())) {
                return;
            }
        }
        System.out.println("ERROR " + file);
    }

    private boolean extractData(String coordinates, Path filename) {

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
            if (strSplit[0].length() > 2) {
                stringBuilder.replace(0, strSplit[0].length(), strSplit[0].replace(" ", ","));
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
                    redis.write("row_" + filename.getFileName(),stringBuilder.toString());
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
        replaceAll(stringBuilder, "\"\",,,,,,,HC*,\n", "");
        replaceAll(stringBuilder, "\"\",,,,,,,HC*,,\n", "");
        replaceAll(stringBuilder, "\"\",,,,,,HC*,,\n", "");
        replaceAll(stringBuilder, "\"\",,,,,HC*,,\n", "");
        replaceAll(stringBuilder, "\"\",,,,,HC*\n", "");
        replaceAll(stringBuilder, "\"\",,,,,,HC*\n", "");
        replaceAll(stringBuilder, "\"\",,,,,,HC*,,,\n", "");
        replaceAll(stringBuilder, "\"\",,,,,,,HC*\n", "");
        replaceAll(stringBuilder, "\"\",,,,,,HC*,\n", "");
        replaceAll(stringBuilder, "\"\",,,,,HC*,\n", "");
        replaceAll(stringBuilder, "\"\",,,,,Ergebnis ohne HC*,,\n", "");
        replaceAll(stringBuilder, "Ergebnis ohne HC*", ",");
        replaceAll(stringBuilder, "Ergebnis mit HC*", "");


    }

    public static void replaceAll(StringBuilder stringBuilder, String from, String to) {
        int index = stringBuilder.indexOf(from);
        if (index > 0) {
            stringBuilder.replace(index, index + from.length(), to);
        }
    }

}
