package com.bkk.de.gluex;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ArrayUtils.add;

@Component
public class CSV {

    Redis redis;

    DateTimeFormatter formatter8 = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH);
    DateTimeFormatter formatter10 = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    SimpleDateFormat targetFormatter = new SimpleDateFormat("ddMMyy");

    @Autowired
    public CSV(Redis redis) {
        this.redis = redis;
    }

    public void read() {

        try {
            Path path = Paths.get("/home/bkk/gluex/football-data/D1.csv");
            readLineByLine(path).forEach(System.out::println);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public List<String[]> readAllLines(Path filePath) throws Exception {
        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                return csvReader.readAll();
            }
        }
    }

    public List<String[]> readLineByLine(Path filePath) throws Exception {
        List<String[]> list = new ArrayList<>();
        try (Reader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath.toFile().getAbsoluteFile()), "UTF8"))) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = csvReader.readNext()) != null) {
                    list.add(line);
                }
            }
        }
        return list;
    }

    public void importFootballData() throws Exception {
        Path dir = Paths.get("/home/bkk/gluex/football-data/");
        Files.walk(dir)
                .filter(p -> !p.toFile().isDirectory())
                .forEach(path -> {
                    System.out.println(path.toFile().getAbsolutePath());
                    try {
                        Path p = Paths.get(path.toFile().getAbsolutePath());
                        importFootballData(p);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void importFootballData(Path path) throws Exception {
        List<String[]> listData = readLineByLine(path);
        listData
                .stream()
                .filter(l -> !l[0].contains("Div"))
                .forEach(str -> {
                    List<String> resultStr = Arrays.stream(str)
                            .filter(f -> !(f.length() == 5 && f.contains(":")))
                            .map(m -> m.contains("/") && m.length() == 8 ? formatStringToDate(m) : m)
                            .map(m -> m.contains("/") && m.length() == 10 ? formatStringToDate(m) : m)
                            .toList();

                    System.out.println(resultStr);

                    String teamHome = resultStr.get(2).replaceAll(" ", "_").replaceAll("'", "");
                    String teamGuest = resultStr.get(3).replaceAll(" ", "_").replaceAll("'", "");

                    String key = "GAME_" + teamHome + "-" + teamGuest + "-" + resultStr.get(1);
                    key = key.replaceAll("'", "");

//                    if (resultStr.size() != 70) {
//                        throw new RuntimeException("result too short!");
//                    }

                    System.out.println("TEAM_" + teamHome);
                    System.out.println("TEAM_" + teamGuest);
                    redis.write(key, String.join(",", resultStr));
                    redis.write("TEAM_" + teamHome, teamHome);
                    redis.write("TEAM_" + teamGuest, teamGuest);
                });
    }

    public String formatStringToDate(String str) {
        LocalDate dateTime = null;
        if (str.length() == 8) {
            dateTime = LocalDate.parse(str, formatter8);
        }
        if (str.length() == 10) {
            dateTime = LocalDate.parse(str, formatter10);
        }

        String d = targetFormatter.format(java.sql.Date.valueOf(dateTime));

        return d;
    }

    public void readCsv() {
        try {
            List<String> inputStringList = new ArrayList<>();
            inputStringList.add("der Ersatzauslosung");
            inputStringList.add("EM-Qualifikation");
            inputStringList.add("WM-Qualifikation");
            inputStringList.add("EM-Q.");
            inputStringList.add("Freundschafts-Länderspiel");
            inputStringList.add("FA-Cup Viertelfinale");
            inputStringList.add("FA-Cup Viertelfinale");
//            inputStringList.add("Sieger ");
            List<List<String[]>> list = new ArrayList<>();
            AtomicInteger atomicInteger = new AtomicInteger(0);
            Tools.getAllFilesFromDir("/home/bkk/gluex/side10/csv")
                    .forEach(p -> {
                        try {

//                            List<String[]> filename = new ArrayList<>();
//                            filename.add(new String[]{p.getFileName().toString()});
//                            list.add(0, filename);
                            list.add(readLineByLine(p));
                            list.get(list.size()-1).add(new String[]{p.getFileName().toString()});

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            list
                    .stream()
                    .filter(f -> {
                        List<String> str = f.stream().map(a -> Arrays.toString(a)).toList();
                        String res = str.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
                        return inputStringList.stream().noneMatch(l -> res.contains(l));
                    })
                    .forEach(stringArray -> {
                        atomicInteger.set(0);
                        stringArray
                                .stream()
                                .filter(t -> Tools.isNumeric(t[0]))
                                .filter(t -> !t[1].isEmpty())
                                .filter(t -> !Tools.isNumeric(t[1]))
                                .filter(t -> t.length > 25)
                                .filter(t -> !Tools.isNumeric(t[1]))
                                .filter(t -> !t[1].equals(""))
                                .forEach(t -> {

                                    String result = Arrays.toString(t).replace("\n", "").replace("[", "").replace("]", "");
                                    String key = Tools.getKey("GLUEX", stringArray.get(stringArray.size()-1)[0]).replace(".csv","-" + atomicInteger.addAndGet(1));
                                    System.out.println(key + " <> " + atomicInteger.get() + " <> " + t.length + " <> " + result);
                                    redis.write(key, result);
                                });

                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}



