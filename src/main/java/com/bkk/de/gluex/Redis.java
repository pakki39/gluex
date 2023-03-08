package com.bkk.de.gluex;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public long deleteKey(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.del(key);
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

    public Set<String> getAllTeams() {
        Set<String> retList = new HashSet<>();
        getKeys("TEAM_*")
                .forEach(key -> retList.add(getKey(key)));
        return retList;
    }

    public List<String> getAllGamesWithTeam(String team, String date) {
        return getKeys("GAME_*" + team.replace("TEAM_","") + "*" + date + "*")
                .stream()
                .toList();

    }

    public String getGluexTeamId(String team) {
        return getKey("TEAM_GLUEX_" + team.replace(" ","_"));
    }

    public void showAllGamesFromGluex() {
        getKeys("GLUEX-*")
                .forEach(k -> {
                    System.out.println(getKey(k));
                });
    }

    public static void main(String[] args) {
        Redis redis = new Redis();
        redis.showAllGamesFromGluex();
    }


}
