package com.bkk.de.gluex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Neuronal {

    Redis redis;
    List<List<String>> trainList;

    public Neuronal(Redis redis) {
        this.redis = redis;
        trainList = new ArrayList<>();
    }

    public void getRows() {
        StringBuilder stringBuilder = new StringBuilder();
        String key = "ROW-2018-01";
        addScoreStringBuilder(getScore(key.replace("ROW", "VA")), stringBuilder);
        redis.getKeys(key).forEach(k -> {
            Arrays.asList(redis.getKey(k).split("\n")).forEach(r -> {
                convertBet(r, k, stringBuilder);
            });
            String strResult = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
            redis.write(key.replace("ROW", "RES"), strResult);
            System.out.println("\n" + strResult);
        });
    }

    public void convertBet(String strBet, String key, StringBuilder stringBuilder) {
        try {

            String strSplit[] = strBet.split(",");
            addGameNr(strSplit[0], stringBuilder);
            addScoreStringBuilder(addWeekDay(strSplit[1]), stringBuilder);
            addScoreStringBuilder(addPlacement(strSplit[2]), stringBuilder);

            System.out.println(stringBuilder.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getScore(String key) {
        return String.join(",", redis.getScore(key));
    }

    private void corrections(String stringScore) {
        String str = stringScore.split(",")[0];
        if (!isNumeric(str)) {
            str = str.replace(" ", ",");
        }
        stringScore.replaceFirst(stringScore.split(",")[0], str);
    }

    private void addGameNr(String str, StringBuilder result) throws Exception {
        if (!isNumeric(str)) {
            String strSplit[] = str.split(" ");
            if (strSplit[0].length() < 3) {
                addScoreStringBuilder(str, result);
                return;
            } else {
                throw new Exception("addGameNr" + str);
            }
        }
        addScoreStringBuilder(str, result);
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

    private String addPlacement(String str) {
//        try {
//            String strSplit[] = str.split("-");
//            Pattern p = Pattern.compile("\\(\\d+\\)");
//            Matcher m = p.matcher(strSplit[0]);
//            String pos1 = "100";
//            if (m.find()) {
//                pos1 = m.group();
//            }
//            String pos2 = "100";
//            m = p.matcher(strSplit[1]);
//            if (m.find()) {
//                pos2 = m.group();
//            }
//
//            String team1 = strSplit[0].replace(pos1, "").trim();
//            String team2 = strSplit[1].replace(pos2, "").trim();
//
//            String teamId1 = redis.getTeamId(team1);
//            String teamId2 = redis.getTeamId(team2);
//
//            if (teamId1 == null || teamId2 == null || pos1.isEmpty() || pos2.isEmpty()) {
//                throw new Exception("addPlacement: \n" + team1 + " " + teamId1 + " \n" + team2 + " " + teamId2 + " \n" + pos1 + " " + pos2);
//            }
//
//            return String.join(",", new ArrayList<>(List.of(teamId1, pos1.replace("(", "").replace(")", ""),
//                    teamId2, pos2.replace("(", "").replace(")", ""))));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        return "";
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

    private void addScoreStringBuilder(String str, StringBuilder stringBuilder) {
        stringBuilder.append(str);
        stringBuilder.append(",");
    }


}
