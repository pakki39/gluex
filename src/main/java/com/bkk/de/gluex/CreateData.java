package com.bkk.de.gluex;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class CreateData {

    Redis redis;
    List<List<String>> trainList;

    public CreateData(Redis redis) {
        this.redis = redis;
        trainList = new ArrayList<>();
    }

    public void createData() {
        redis.getKeys("GLUEX-*")
                .stream()
                .sorted()
                .map(s -> {
                    String strSplit[] = s.split("-");
                    return strSplit[0] + "-" + strSplit[1] + "-" + strSplit[2];
                })
                .collect(Collectors.toSet()).stream().sorted().forEach(k -> createDataFromGluex(k));
    }
    public void createDataFromGluex(String key) {
        List<String> resultStr = new ArrayList<>();
        System.out.println(key);
        setScore(key, resultStr);
        setDataPerGame(key, resultStr);


        System.out.println(resultStr);
        System.out.println("");


//        redis.getKeys(key).forEach(k -> {
//            Arrays.asList(redis.getKey(k).split("\n")).forEach(r -> {
//                convertBet(r, k, stringBuilder);
//            });
//            String strResult = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
//            redis.write(key.replace("ROW", "RES"), strResult);
//            System.out.println("\n" + strResult);
//        });
    }



    public void setDataPerGame(String key, List<String> resultList) {
        try {
            String strSplit[] = key.split("-");
            String rediskey = "GLUEX-" + strSplit[1] + "-" + strSplit[2] + "*";
            redis.getKeys(rediskey)
                    .stream()
                    .sorted(Comparator.comparing(p -> Integer.valueOf(p.split("-")[3])))
                    .forEach(k -> {
                        String str = redis.getKey(k);
                        addGameNr(str, resultList);
                        addWeekDay(str, resultList);
                    });

//            String strSplit[] = strBet.split(",");
//            addGameNr(strSplit[0], stringBuilder);
//            addScoreStringBuilder(addWeekDay(strSplit[1]), stringBuilder);
//            addScoreStringBuilder(addPlacement(strSplit[2]), stringBuilder);
//            addScoreStringBuilder(addBettingOdds(strSplit[3]), stringBuilder);
//
//            System.out.println(stringBuilder.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String addBettingOdds(String s) {
        if(s.length() != 5) {
            throw  new RuntimeException("BettingOdds");
        }
        return "1";
    }

    private void setScore(String key, List<String> resList) {
        try {
            String strSplit[] = key.split("-");
            String rediskey = "VA-" + strSplit[1] + "-" + strSplit[2];
            String redisRes = redis.getKey(rediskey);
            if(redisRes == null || redisRes.isEmpty()) {
                System.out.println("ERROR no results found for key: " + key);
                return;
            }
            List<String> list = Arrays.asList(redisRes.split(","));
            IntStream.range(0,list.size())
                    .filter(index -> index > 1 && index < 8)
                    .mapToObj(index -> list.get(index))
                    .collect(Collectors.toList()).stream().sorted(Comparator.comparing(Integer::valueOf)).forEach(s -> resList.add(s));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addGameNr(String str, List<String> resultStr) {
        resultStr.add(str.split((","))[0]);
    }

    private void addWeekDay(String str, List<String> resulStr) {
        switch (str.split(",")[1].trim()) {
            case "MO":
                resulStr.add("1") ; break;
            case "DI":
                resulStr.add("2") ; break;
            case "MI":
                resulStr.add("3") ; break;
            case "DO":
                resulStr.add("4") ; break;
            case "FR":
                resulStr.add("5") ; break;
            case "SA":
                resulStr.add("6") ; break;
            case "SO":
                resulStr.add("7") ; break;
            default:
                resulStr.add("8") ;
        }
    }

    private String setStandings(String str, List<String> resultStr) {
        try {
            String strSplit[] = str.split(" - ");
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

            if(str.contains("Mother")) {
                System.out.println("");
            }

            String team1 = strSplit[0].replace(pos1, "").replace("*","").replace("fi eld","field").trim();
            String team2 = strSplit[1].replace(pos2, "").replace("*","").replace("fi eld","field").trim();

            String teamId1 = redis.getGluexTeamId(team1);
            String teamId2 = redis.getGluexTeamId(team2);

            if (teamId1 == null || teamId2 == null || pos1.isEmpty() || pos2.isEmpty()) {
                throw new Exception("addPlacement: \n" + team1 + " " + teamId1 + " \n" + team2 + " " + teamId2 + " \n" + pos1 + " " + pos2);
            }

            return String.join(",", new ArrayList<>(List.of(teamId1, pos1.replace("(", "").replace(")", ""),
                    teamId2, pos2.replace("(", "").replace(")", ""))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void addScoreStringBuilder(String str, StringBuilder stringBuilder) {
        stringBuilder.append(str);
        stringBuilder.append(",");
    }

    public void improveDataQuality() {

        redis.getKeys("ROW-*")
                .stream()
                .forEach(key -> {
                    String rowStr = redis.getKey(key);
                    deleteBadRows(rowStr,key);
                    String newStr = improveGameNr(rowStr);
                    redis.write(key, newStr);
                });

        redis.getKeys("ROW-*").forEach(key -> {
            System.out.println(key);
            System.out.println(redis.getKey(key));
        });

    }

    private String improveGameNr(String rowStr) {
        return Arrays.asList(rowStr.split("\n"))
                .stream()
                .map(s -> {
                    List<String> arrList = Arrays.asList(s.split(","));
                    if(arrList.get(0).contains("HC*")) {
                        System.out.println("before: " + arrList.get(0));
                        arrList.set(0, arrList.get(0).replace("HC*",""));
                        System.out.println("after: " + arrList.get(0));
                    }
                    if(!Tools.isNumeric(arrList.get(0))) {
                        System.out.println("before: " + arrList.get(0));
                        arrList.set(0,arrList.get(0).replace(" ", ","));
                        System.out.println("after: " + arrList.get(0));
                    }
                    return String.join(",",arrList);
                })
                .collect(Collectors.joining("\n"));
    }

    private void deleteBadRows(String rowStr, String key) {
        if(rowStr.contains("Ersatzauslosung")) {
            redis.deleteKey(key);
            System.out.println("Key: " + key + " deleted!");
        }
        if(rowStr.contains("Erster Gruppe")) {
            redis.deleteKey(key);
            System.out.println("Key: " + key + " deleted!");
        }
        if(rowStr.contains("Finale")) {
            redis.deleteKey(key);
            System.out.println("Key: " + key + " deleted!");
        }
    }

}
