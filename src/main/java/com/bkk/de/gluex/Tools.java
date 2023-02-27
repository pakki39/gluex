package com.bkk.de.gluex;

import org.springframework.stereotype.Component;

import java.util.Arrays;

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

        System.out.println("KEY: " + newKey);

        return newKey;
    }

}
