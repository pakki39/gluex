package com.bkk.de.gluex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Neuronal {

    Redis redis;
    List<List<String>> trainList;

    public Neuronal(Redis redis) {
        this.redis = redis;
        trainList = new ArrayList<>();
    }

    public void getRows() {
        redis.getKeys("row_*").forEach(k -> {
            convertBet(redis.getKey(k), k);

        });
    }

    public void convertBet(String strBet, String key) {
        setKey(strBet, key);
    }

    private void setKey(String strBet, String key) {
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

        String newKey = "id_" + year + week;

        System.out.println(key + " >>> " + newKey);

        redis.write(newKey, strBet);
    }


    private void corrections(String stringScore) {
        String str = stringScore.split(",")[0];
        if (!isNumeric(str)) {
            str = str.replace(" ", ",");
        }
        stringScore.replaceFirst(stringScore.split(",")[0], str);
    }

    private String addGameNr(String str) throws Exception {
        if (!isNumeric(str)) {
            String strSplit[] = str.split(" ");
            if (strSplit[0].length() < 3) {
                return str;
            } else {
                throw new Exception("addGameNr" + str);
            }
        }
        return str;
    }

    private String addWeekDay(String str) {
        switch (str) {
            case "MO":
                return "1";
            case "DI":
                return "2";
            case "MI":
                return "3";
            case "DO":
                return "4";
            case "FR":
                return "5";
            case "SA":
                return "6";
            case "SO":
                return "7";
            default:
                return "8";
        }
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


}
