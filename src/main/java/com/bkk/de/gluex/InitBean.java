package com.bkk.de.gluex;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InitBean implements InitializingBean {

    PDF pdf;
    CreateData createData;

    Excel excel;

    FootballDB footballDB;
    CSV csv;

    ExtractData extractData;

    Redis redis;
    FuzzyMatcher fuzzyMatcher;

    @Autowired
    public InitBean(PDF pdf, CreateData createData, Excel excel, FootballDB footballDB, CSV csv, ExtractData extractData, Redis redis, FuzzyMatcher fuzzyMatcher) {
        this.pdf = pdf;
        this.createData = createData;
        this.excel = excel;
        this.footballDB = footballDB;
        this.csv = csv;
        this.extractData = extractData;
        this.redis = redis;
        this.fuzzyMatcher = fuzzyMatcher;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

//        excel.read("/home/bkk/gluex/Auswahlwette1975.xlsx");
//        excel.readNewer("/home/bkk/gluex/AW_ab_2018.xlsx");

//        createData.createData();

//        fuzzyMatcher.start();

//        csv.importFootballData();
//        redis.getTeamNameId("Manchester City");
//        redis.getTeamNameId("Manchester United");
//extractData.getTeam("1.FC Union Berlin");
//        redis.getTeamNameId("Borussia Dortmund");
//        redis.getTeamNameId("Borussia Mönchengladbach");
//        redis.getTeamNameId("CFC Genua 1893");


//        extractData.extractTeams();


//        footballDB.loopPersistTeams();

//        final long calendarWeek = 20;
//        LocalDate ltest = LocalDate.now();
//        ltest = ltest.withYear(2022);
//        LocalDate desiredDate = ltest
//                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, calendarWeek)
//                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
//
//        System.out.println(desiredDate);
//
//        redis.getAllGamesWithTeam(fuzzyMatcher.getTeamId("AS Saint-Etienne").getValue(),"0522")
//                        .forEach(s -> System.out.println(s));

//    createData.improveDataQuality();


//        Tools.getAllFilesFromDir("/home/bkk/gluex/pdf")
//                .forEach(f -> {
//                    System.out.println(f.getFileName());
//                    pdf.scanGluexPDF(f);
//                });

        csv.readCsv();
    }

}
