package com.bkk.de.gluex;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@Component
public class FootballDB {

    Redis redis;

    @Autowired
    public FootballDB(Redis redis) {
        this.redis = redis;
    }

    public void persistLeagues() throws IOException {

        String requestStr = "";
        try {
            HttpUtility httpUtility = new HttpUtility();
            httpUtility.sendGetRequest("https://api.openligadb.de/getavailableleagues");
            requestStr = httpUtility.readSingleLineRespone();
            System.out.println(requestStr);
        } catch (IOException e) {
            System.out.println("NO Response for https://api.openligadb.de/getavailableleagues");
            return;
        }


        new JSONArray(requestStr).forEach(str -> {
//            System.out.println(str);
            JSONObject sport = (JSONObject) ((JSONObject) str).get("sport");
            String sportName = ((String) sport.get("sportName")).trim();
            String leagueName = (String) ((JSONObject) str).get("leagueName");
            System.out.println(leagueName);
//            if (sportName.equals("Fußball")
//                    && !leagueName.toLowerCase().contains("xxx")
//                    && !leagueName.toLowerCase().contains("kick")
//                    && !leagueName.toLowerCase().contains("tipp")
//                    && !leagueName.toLowerCase().contains("bezirksliga")
//                    && !leagueName.toLowerCase().contains("kreis")
//                    && !leagueName.toLowerCase().contains("brinkwerth")
//                    && !leagueName.toLowerCase().contains("triedaliptovmuži")
//                    && !leagueName.toLowerCase().contains("hockey")
//                    && !leagueName.toLowerCase().contains("hackminton")
//                    && !leagueName.toLowerCase().contains("test")
//                    && !leagueName.toLowerCase().contains("stadtmeisterschaft")
//                    && !leagueName.toLowerCase().contains("adeccoligaen")
//                    && !leagueName.toLowerCase().contains("alte herren")
//                    && !leagueName.toLowerCase().contains("anameñade")
//                    && !leagueName.toLowerCase().contains("Axpo".toLowerCase())
//                    && !leagueName.toLowerCase().contains("Oberliga Ndb".toLowerCase())
//                    && !leagueName.toLowerCase().contains("frutti")
//                    && !leagueName.toLowerCase().contains("comunio")
//                    && !leagueName.toLowerCase().contains("botola")
//                    && !leagueName.toLowerCase().contains("eirepromotioun")
//                    && !leagueName.toLowerCase().contains("aknm")
//                    && !leagueName.toLowerCase().contains("süd 3 rr")
//                    && !leagueName.toLowerCase().contains("nektarios")
//                    && !leagueName.toLowerCase().contains("swwm")
//                    && !leagueName.toLowerCase().contains("dummy")
//                    && !leagueName.toLowerCase().contains("TIPP")
//                    && !leagueName.toLowerCase().contains("sdeccoligaen")
//                    && !leagueName.toLowerCase().contains("aknm")
//                    && !leagueName.toLowerCase().contains("swem")
//                    && !leagueName.toLowerCase().contains("aaa")
//                    && !leagueName.toLowerCase().contains("myfifa")
//                    && !leagueName.toLowerCase().contains("monsch")
//                    && !leagueName.toLowerCase().contains("rsl-davi")
//                    && !leagueName.toLowerCase().contains("merian-liga")
//                    && !leagueName.toLowerCase().contains("knippsa")
//                    && !leagueName.toLowerCase().contains("felix")
//                    && !leagueName.toLowerCase().contains("spaß")
//                    && !leagueName.toLowerCase().contains("arenen")
//                    && !leagueName.toLowerCase().contains("jugend")
//                    && !leagueName.toLowerCase().contains("myliga")
//                    && !leagueName.toLowerCase().contains("ligaaa")
//                    && !leagueName.toLowerCase().contains("landes")
//                    && !leagueName.toLowerCase().contains("teil2")
//                    && !leagueName.toLowerCase().contains("löschen")
//                    && !leagueName.toLowerCase().contains("fusball")
//                    && !leagueName.toLowerCase().contains("goalunit")
//                    && !leagueName.toLowerCase().contains("vogelwm")
//                    && !leagueName.toLowerCase().contains("#usg")
//                    && !leagueName.toLowerCase().contains("***")
//                    && !leagueName.toLowerCase().contains("arabien")
//                    && !leagueName.toLowerCase().contains("e3.liga")
//                    && !leagueName.toLowerCase().contains("1860")
//                    && !leagueName.toLowerCase().contains("mitte-b")
//                    && !leagueName.toLowerCase().contains("sudheer")
//                    && !leagueName.toLowerCase().contains("gbwm")
//                    && !leagueName.toLowerCase().contains("sima")
//                    && !leagueName.toLowerCase().contains("dynamo")
//                    && !leagueName.toLowerCase().contains("gudefifa")
//                    && !leagueName.toLowerCase().contains("bundesliega")
//                    && !leagueName.toLowerCase().contains("beerwars")
//                    && !leagueName.toLowerCase().contains("tabelle")
//                    && !leagueName.toLowerCase().contains("mitteost")
//                    && !leagueName.toLowerCase().contains("raiffeisen")
//                    && !leagueName.toLowerCase().contains("gruppemliga")
//                    && !leagueName.toLowerCase().contains("kamenz")
//                    && !leagueName.toLowerCase().contains("bundesligamoe")
//                    && !leagueName.toLowerCase().contains("vokabel")
//                    && !leagueName.toLowerCase().contains("gebiet")
//                    && !leagueName.toLowerCase().contains("agenda")
//                    && !leagueName.toLowerCase().contains("nm-te")
//                    && !leagueName.toLowerCase().contains("wasd")
//                    && !leagueName.toLowerCase().contains("20csmp16")
//                    && !leagueName.toLowerCase().contains("die_gute")
//                    && !leagueName.toLowerCase().contains("b-klasse")
//                    && !leagueName.toLowerCase().contains("junior")
//                    && !leagueName.toLowerCase().contains("elmliga")
//                    && !leagueName.toLowerCase().contains("bunteliga")
//                    && !leagueName.toLowerCase().contains("burgenland")
//                    && !leagueName.toLowerCase().contains("!!!")
//                    && !leagueName.toLowerCase().contains("Turnier")
//                    && !leagueName.toLowerCase().contains("dhbw15d")
//                    && !leagueName.toLowerCase().contains("wmru2018")
//                    && !leagueName.toLowerCase().contains("battle")
//                    && !leagueName.toLowerCase().contains("msmvdl")
//                    && !leagueName.toLowerCase().contains("level")
//                    && !leagueName.toLowerCase().contains("fantasy")
//                    && !leagueName.toLowerCase().contains("dsawelt")
//                    && !leagueName.toLowerCase().contains("- davi")
//                    && !leagueName.toLowerCase().contains("ddb691")
//                    && !leagueName.toLowerCase().contains("myliga")
//                    && !leagueName.toLowerCase().contains("virtuel")
//                    && !leagueName.toLowerCase().contains("herrennvp")
//                    && !leagueName.toLowerCase().contains("essen1")
//                    && !leagueName.toLowerCase().contains("trieda")
//                    && !leagueName.toLowerCase().contains("swisttal")
//                    && !leagueName.toLowerCase().contains("tipico")
//
//            ) {

                String leagueShortcut = (String) ((JSONObject) str).get("leagueShortcut");

                String key = "LEAGUE_" + leagueName.trim().replaceAll(" ", "_");
                redis.write(key, leagueShortcut.trim());

                System.out.println(">>> " + key);

//            }

        });

    }

    public void loopPersistTeams() {
        redis.getKeys("LEAGUE_*")
                .forEach(l -> {
                    System.out.println(l);
                    System.out.println(redis.getKey(l));
                    try {
                        for (int i = 2015; i < 2024; i++) {
                            persistTeams(redis.getKey(l), String.valueOf(i));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
//        try {
//            persistTeams("bl1","2022");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void persistTeams(String leagueShortcut, String leagueSeason) throws IOException {

        String requestStr = "";
        try {
            HttpUtility httpUtility = new HttpUtility();
            httpUtility.sendGetRequest("https://api.openligadb.de/getavailableteams/" + leagueShortcut + "/" + leagueSeason);
            requestStr = httpUtility.readSingleLineRespone();
            System.out.println(requestStr);
        } catch (IOException e) {
            System.out.println("NO Response for leagueShorcut: " + leagueShortcut + " leagueSeason: " + leagueSeason);
            return;
        }

        new JSONArray(requestStr).forEach(str -> {
            System.out.println(str);
            Integer teamId = (Integer) ((JSONObject) str).get("teamId");
            String teamName = (String) ((JSONObject) str).get("teamName");
            String key = "TEAM_" + teamName.trim().replaceAll(" ", "_").replaceAll("'","");
            redis.write(key, String.valueOf(teamId));
            System.out.println(key + " " + teamId);
        });

    }

}

