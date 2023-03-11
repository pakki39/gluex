package com.bkk.de.gluex;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class CreateData {

    FuzzyMatcher fuzzyMatcher;
    Redis redis;
    List<List<String>> trainList;
    Map<Integer, String> resultMap;

    @Autowired
    public CreateData(Redis redis, FuzzyMatcher fuzzyMatcher) {
        this.redis = redis;
        this.fuzzyMatcher = fuzzyMatcher;
        trainList = new ArrayList<>();
        resultMap = new TreeMap<>();
    }

    public void createData() {
        redis.getKeys("GLUEX-*")
                .forEach(this::createDataFromGluex);
        resultMap
                .entrySet()
                .forEach(entry -> System.out.println(entry.getKey() + " <> " + entry.getValue()));

    }

    private Integer createKey(String p) {
        String[] strSplit = p.split("-");
        String orderStr = "0" + strSplit[3];
        return Integer.valueOf(strSplit[1] + strSplit[2] + orderStr.substring(orderStr.length() - 2));
    }

    private void addResultStr(String key, String result) {
        Integer iKey = createKey(key);
        if (resultMap.containsKey(iKey)) {
            result = resultMap.get(iKey) + "," + result;
            resultMap.put(iKey, result);
        } else {
            resultMap.put(iKey, result);
        }
    }

    public void createDataFromGluex(String key) {
        String redisStr = redis.getKey(key);
        setScore(key);
        addGameNr(key, redisStr);
        addWeekDay(key, redisStr);
        addStandings(key, redisStr);
        addBetQuotes(key, redisStr);
        addLastGames(key, redisStr);
    }


    private String addBettingOdds(String s) {
        if (s.length() != 5) {
            throw new RuntimeException("BettingOdds");
        }
        return "1";
    }

    private void setScore(String key) {
        try {
            String[] strSplit = key.split("-");
            String rediskey = "VA-" + strSplit[1] + "-" + Integer.valueOf(strSplit[2]);
            String redisRes = redis.getKey(rediskey);
            if (redisRes == null || redisRes.isEmpty()) {
//                System.out.println("ERROR no results found for key: " + key);
                return;
            }
            List<String> list = Arrays.asList(redisRes.split(","));
            List<String> res = new ArrayList<>();
            IntStream.range(0, list.size())
                    .filter(index -> index > 1 && index < 8)
                    .mapToObj(list::get)
                    .toList().stream().sorted(Comparator.comparing(Integer::valueOf)).forEach(s -> res.add(s));
            String[] keySplit = key.split("-");
            String mapKey = keySplit[0] + "-" + keySplit[1] + "-" + keySplit[2] + "-0";
            resultMap.put(createKey(mapKey), String.join(",", res));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addGameNr(String key, String str) {
        addResultStr(key, str.split((","))[0]);
    }

    private void addWeekDay(String key, String result) {
        switch (result.split(",")[1].trim()) {
            case "MO" -> addResultStr(key, "1");
            case "DI" -> addResultStr(key, "2");
            case "MI" -> addResultStr(key, "3");
            case "DO" -> addResultStr(key, "4");
            case "FR" -> addResultStr(key, "5");
            case "SA" -> addResultStr(key, "6");
            case "SO" -> addResultStr(key, "7");
            default -> addResultStr(key, "8");
        }
    }

    @SneakyThrows
    private void addBetQuotes(String key, String result) {
        String betStr;
        try {
            betStr = result.split(",")[3];
            String[] strSplit = betStr.split("-");
            addResultStr(key, String.valueOf(Integer.valueOf(strSplit[0].trim()) * 100));
            addResultStr(key, String.valueOf(Integer.valueOf(strSplit[1].trim()) * 100));
            addResultStr(key, String.valueOf(Integer.valueOf(strSplit[2].trim()) * 100));
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private void addLastGames(String key, String result) {
        String lastGamesStr;
        try {
            String[] strSplit = result.split(",");
            String[] lastGamesStrArray = Arrays.copyOfRange(strSplit, 4, 7);
            Arrays.stream(lastGamesStrArray)
                            .forEach(s -> {
                                if(s.trim().equals("-")) {
                                    addResultStr(key, "1");
                                    addResultStr(key, "1");
                                } else {
                                    String[] resArray = s.split(":");
                                    if(resArray[0].trim().equals("0")) {
                                        addResultStr(key, "10");
                                    } else {
                                        addResultStr(key, String.valueOf(Integer.valueOf(resArray[0])*100));
                                    }
                                    if(resArray[1].trim().equals("0")) {
                                        addResultStr(key, "10");
                                    } else {
                                        addResultStr(key, String.valueOf(Integer.valueOf(resArray[1])*100));
                                    }


                                }
                            });
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

    }

    private void addStandings(String key, String str) {
        try {
            String extracted = str.split(",")[2];
            extracted = extracted.replace("\u2013", "-");
            extracted = extracted.replace("- ", " - ");
            String strSplit[] = extracted.split(" - ");
            Pattern p = Pattern.compile("\\(\\s?\\d+\\s?\\)");
            Matcher m = p.matcher(strSplit[0]);
            String pos1 = "100";
            if (m.find()) {
                pos1 = m.group();
            }
            String pos2 = "100";
            m = p.matcher(strSplit[1]);
            if (m.find()) {
                pos2 = m.group();
            }

            String team1 = strSplit[0].replace(pos1, "").replace("*", "").replace("fi eld", "field").trim();
            String team2 = strSplit[1].replace(pos2, "").replace("*", "").replace("fi eld", "field").trim();

            String teamId1 = fuzzyMatcher.getTeamId(team1).getValue().split(",")[1];
            String teamId2 = fuzzyMatcher.getTeamId(team2).getValue().split(",")[1];

            if (teamId1 == null || teamId2 == null || pos1.isEmpty() || pos2.isEmpty()) {
                throw new Exception("addPlacement: \n" + team1 + " " + teamId1 + " \n" + team2 + " " + teamId2 + " \n" + pos1 + " " + pos2);
            }

            addResultStr(key, teamId1);
            addResultStr(key, pos1.replace("(", "").replace(")", ""));
            addResultStr(key, teamId2);
            addResultStr(key, pos2.replace("(", "").replace(")", ""));

//            resultMap.put(createKey(key),String.join(",", new ArrayList<>(List.of(teamId1, pos1.replace("(", "").replace(")", ""),
//                    teamId2, pos2.replace("(", "").replace(")", "")))));
        } catch (Exception e) {
            for (int i = 0; i < str.length(); i++) {
                System.out.println(str.charAt(i) + " <> " + (int) str.charAt(i));
            }
            throw new RuntimeException(e);
        }

    }

    private void addScoreStringBuilder(String str, StringBuilder stringBuilder) {
        stringBuilder.append(str);
        stringBuilder.append(",");
    }

    public void improveDataQuality() {

        redis.getKeys("ROW-*")
                .forEach(key -> {
                    String rowStr = redis.getKey(key);
                    deleteBadRows(rowStr, key);
                    String newStr = improveGameNr(rowStr);
                    redis.write(key, newStr);
                });

        redis.getKeys("ROW-*").forEach(key -> {
            System.out.println(key);
            System.out.println(redis.getKey(key));
        });

    }

    private String improveGameNr(String rowStr) {
        return Arrays.stream(rowStr.split("\n"))
                .map(s -> {
                    List<String> arrList = Arrays.asList(s.split(","));
                    if (arrList.get(0).contains("HC*")) {
                        System.out.println("before: " + arrList.get(0));
                        arrList.set(0, arrList.get(0).replace("HC*", ""));
                        System.out.println("after: " + arrList.get(0));
                    }
                    if (!Tools.isNumeric(arrList.get(0))) {
                        System.out.println("before: " + arrList.get(0));
                        arrList.set(0, arrList.get(0).replace(" ", ","));
                        System.out.println("after: " + arrList.get(0));
                    }
                    return String.join(",", arrList);
                })
                .collect(Collectors.joining("\n"));
    }

    private void deleteBadRows(String rowStr, String key) {
        if (rowStr.contains("Ersatzauslosung")) {
            redis.deleteKey(key);
            System.out.println("Key: " + key + " deleted!");
        }
        if (rowStr.contains("Erster Gruppe")) {
            redis.deleteKey(key);
            System.out.println("Key: " + key + " deleted!");
        }
        if (rowStr.contains("Finale")) {
            redis.deleteKey(key);
            System.out.println("Key: " + key + " deleted!");
        }
    }

}
