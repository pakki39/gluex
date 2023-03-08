package com.bkk.de.gluex;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExtractData {

    Redis redis;
    Map<String, String> strMap;
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
        AtomicInteger atomInt = new AtomicInteger(0);
        allTeamNames.forEach(s -> {
            String key = "TEAM_GLUEX_" + s.replace(" ", "_");
            redis.write(key, String.valueOf(atomInt.addAndGet(1)));
            System.out.println(key);
        });
    }

    public Set<String> getAllTeamsFromGluex() {
        Set<String> listTeamNames = new HashSet<>();
        redis.getKeys("GLUEX-*")
                .forEach(key -> {
                    List<String> list = Arrays.asList(redis.getKey(key).split(","));
                    Tools.extractTeamNames(list.get(2))
                            .forEach(s -> listTeamNames.add(s));


                });
        return listTeamNames;
    }

}
