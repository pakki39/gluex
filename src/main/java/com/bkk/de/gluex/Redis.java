package com.bkk.de.gluex;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Redis {

    JedisPool pool;

    public Redis() {
        pool = new JedisPool("localhost", 6379);
    }

    public void write(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
        }
    }

    public Set<String> getKeys(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.keys(key);
        }
    }


    public String getKey(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    }

    public List<String> getScore(String key) {
        String strSplit[] = key.split("-");
        int year = Integer.valueOf(strSplit[1]);
        int week = Integer.valueOf(strSplit[2]);
        if (week == 1) {
            week = 53;
            year = year - 1;
        }

        String score = getKey("VA-" + year + "-" + week);
        while (score == null && week > 45) {
            score = getKey("VA-" + year + "-" + week);
            week--;
        }

        List<String> retList = new ArrayList<>();

        if (score == null || score.isEmpty()) {
            return retList;
        }
        String retSplit[] = score.split(",");

        retList.add(retSplit[2]);
        retList.add(retSplit[3]);
        retList.add(retSplit[4]);
        retList.add(retSplit[5]);
        retList.add(retSplit[6]);
        retList.add(retSplit[7]);

        return retList;
    }

    public String getTeamId(String team) {
        String key = "TEAM_" + team.replace(" ", "_");
        return getKey(key);
    }

    public String getTeamNameId(String team) {

        List<String> possibleKeys = getPossibleKeys(team);

//        String rteam = team.replace("1.FC", "");
//        rteam = rteam.replace("1. FC", "");
//        rteam = rteam.replace("1. FSV", "");
//        rteam = rteam.replace("05", "");
        team = team.trim().replaceAll(" ", "_");

        Comparator<String> compByLength = (aName, bName) -> bName.length() - aName.length();
        List<String> sortedString = Arrays.asList(team.split("_"))
                .stream().sorted(compByLength).collect(Collectors.toList());

        Optional<String> res = sortedString
                .stream()
                .map(m -> searchTeamId(m, possibleKeys))
                .filter(str -> !str.equals(""))
                .findFirst();

        return res.orElse("") ;

    }

    private List<String> getPossibleKeys(String team) {
        List<String> possibleKeys = new ArrayList<>();
        Arrays.asList(team.split(" "))
                .forEach(str -> {
                    str = str.replace("1.FC", "FC");
                    getKeys("TEAM_*" + str + "*")
                            .forEach(s -> possibleKeys.add(s.replace("TEAM_", "")));
                });
        if (possibleKeys.size() < 2) {
            for (int i = 0; i < team.length() - 3; i++) {
                String subStr = team.substring(i, team.length());
//                System.out.println(subStr);
                getKeys("TEAM_*" + subStr + "*")
                        .forEach(s -> possibleKeys.add(s.replace("TEAM_", "")));
            }
            for (int i = team.length() - 2; i > 2; i--) {
                String subStr = team.substring(0, i);
//                System.out.println(subStr);
                getKeys("TEAM_*" + subStr + "*")
                        .forEach(s -> possibleKeys.add(s.replace("TEAM_", "")));
            }
        }
        return possibleKeys;
    }

    private String searchTeamId(String sString, List<String> possibleKeys) {
        for (int i = 0; i < sString.length(); i++) {
            String subStr = sString.substring(i, sString.length());
//            System.out.println(subStr);
            String result = possibleKeys.stream().filter(s -> s.contains(subStr)).findFirst().orElse("");

            if (!result.isEmpty()) {
                return "TEAMS_" + result;
            }
        }
        for (int i = sString.length() - 1; i > 1; i--) {
            String subStr = sString.substring(0, i);
//            System.out.println(subStr);
            String result = possibleKeys.stream().filter(s -> s.contains(subStr)).findFirst().orElse("");

            if (!result.isEmpty()) {
//                System.out.println("GEFUNDEN: TEAMS_" + result + " >> " + sString + " >>>> " + subStr);
                return "TEAMS_" + result;
            }
        }
        return "";
    }
}
