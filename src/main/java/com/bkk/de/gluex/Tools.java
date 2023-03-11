package com.bkk.de.gluex;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Tools {

    public static String getKey(String prefix, String key) {
        String year = Arrays.asList(key.split("_"))
                .stream()
                .filter(k -> k.charAt(0) == 50 && k.length() == 4)
                .findFirst()
                .orElse("");

        if (year.isEmpty()) {
            year = Arrays.asList(key.split("-"))
                    .stream()
                    .filter(k -> k.charAt(0) == 50 && k.replace(".pdf", "").length() == 4)
                    .findFirst()
                    .orElse("").replace(".pdf", "");
        }

        String week = Arrays.asList(key.split("_"))
                .stream()
                .filter(k -> k.substring(0, 2).equals("kw"))
                .findFirst()
                .orElse("").replace("kw", "").replace(".pdf", "");

        if (week.isEmpty()) {
            week = Arrays.asList(key.split("-"))
                    .stream()
                    .filter(k -> k.substring(0, 2).equals("nr"))
                    .findFirst()
                    .orElse("").replace("nr", "");
        }

        String newKey = prefix + "-" + year + "-" + week;

//        System.out.println("KEY: " + newKey);

        return newKey;
    }

    public static List<String> extractTeamNames(String teamsStr) {

        String splitString;
//        for(int i = 0; i < teamsStr.length(); i++) {
//            System.out.println(teamsStr.substring(i,i+1) + " " + (int) teamsStr.charAt(i));
//        }

        teamsStr = teamsStr.replace("\u2013","-");
        teamsStr = teamsStr.replace("*","");
        teamsStr = teamsStr.replace("fi eld","field");
        teamsStr = teamsStr.replace("Fi eld","Field");
        teamsStr = teamsStr.trim();

        if(teamsStr.contains(" - ")) {
            splitString = " - ";
        } else {
            splitString = "-";
        }

        return Arrays.asList(teamsStr.split(splitString))
                .stream()
                .map(m -> teamMatcher(m))
                .toList();
    }

    public static String teamMatcher(String str) {
        Pattern p = Pattern.compile("\\(\\s?\\d+\\s?\\)");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return str.replace(m.group(), "").replace("*","").trim();
        }
        return str.trim();
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static Set<Path> getAllFilesFromDir(String dir) throws IOException {
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

}
