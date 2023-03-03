package com.bkk.de.gluex;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Component
public class FuzzyMatcher {

    ExtractData extractData;
    List<String> compareStrList;
    Redis redis;

    @Autowired
    public FuzzyMatcher(ExtractData extractData, Redis redis) {
        compareStrList = new ArrayList<>();
        this.extractData = extractData;
        this.redis = redis;
    }


    public void start() {
        List<String> list = new ArrayList<>(extractData.getAllTeamsFromGluex());
        list
                .forEach(s -> {
                    AbstractMap.SimpleEntry<Integer, String> result = getTeamId(s);
//                    System.out.println(s + " <> " + result.getValue());
                    if (result.getKey() > 74) {
                        System.out.println(s + " >>>>>>> " + result.getValue());
                    }
                });
    }

    public AbstractMap.SimpleEntry<Integer, String> getTeamId(String str) {
        fill_compareStrList();
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        compareStrList
                .forEach(s -> treeMap.put(FuzzySearch.ratio(s, str), s));
        return new AbstractMap.SimpleEntry<>(treeMap.lastEntry().getKey(), "TEAM_" + treeMap.lastEntry().getValue());
    }

    private void fill_compareStrList() {
        if (compareStrList.isEmpty()) {
            compareStrList.addAll(redis.getAllTeams());
        }
    }

}
