package com.bkk.de.gluex;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ExtractData {

    Redis redis;
    Map<String,String> strMap;
    List<String> alistFaults = new ArrayList<>();

    public ExtractData(Redis redis) {
        this.redis = redis;
        strMap = new HashMap<>();
        fillTeamMap();
    }

    private void fillTeamMap() {
        strMap.put("CFC Genua 1893", "TEAM_Genoa");
    }

    public void extractTeams() {
        Set<String> allTeamNames = getAllTeamsFromGluex();
        allTeamNames.forEach(s -> System.out.println(s));
        allTeamNames
                .forEach(team -> copyTeams(team));
        alistFaults
                .forEach(System.out::println);
    }

    private Set<String> getAllTeamsFromGluex() {
        Set<String> listTeamNames = new HashSet<>();
        redis.getKeys("ROW-*")
                .forEach(key -> {
                    Arrays.asList(redis.getKey(key).split("\n"))
                            .forEach(l -> {
                                if(l.contains("Ersatzauslosung")) {
                                    return;
                                }
                                if(l.contains("MalmFöo")) {
                                    System.out.println(l);
                                }
                                List<String> list = Arrays.asList(l.split(","));
                                if(list.get(0).length() > 2) {
                                    Tools.extractTeamNames(list.get(1))
                                                    .forEach(s -> listTeamNames.add(s));
                                } else {
                                    Tools.extractTeamNames(list.get(2))
                                            .forEach(s -> listTeamNames.add(s));
                                }
                            });
                });
        return listTeamNames;
    }

    private void copyTeams(String team) {

        List<String> listStr = Tools.extractTeamNames(team);
        if(listStr.size() != 2) {
            throw new RuntimeException("Number of Teams != 2");
        }

        listStr.forEach(l -> getTeam(l));

//        redis.getKeys("Team_*")
    }

    public void getTeam(String team) {
        String teamKey = "TEAM_" + team.replaceAll(" ", "_");
        String resTeam = getManualTeamId(team);
        if(resTeam != null && !resTeam.isEmpty()){
            System.out.println(teamKey + " <> " + getManualTeamId(team));
            return;
        }

        resTeam = redis.getTeamNameId(team);

        if(resTeam.isEmpty()){
            alistFaults.add(team);
//            throw new RuntimeException("no team id found!");
        } else {
            System.out.println(teamKey + " <> " + resTeam);
        }
    }

    private String getManualTeamId(String team) {
        return strMap.get(team);
    }

}
