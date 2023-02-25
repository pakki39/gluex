package com.bkk.de.gluex;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Neuronal {

    List<List<String>> trainList;

    public Neuronal() {
        trainList = new ArrayList<>();
    }

    public void insertTrainList(String stringScore) {
        List<String> listInt = new ArrayList<>();
//        for(String str : stringScore.split("\n")) {
//            List<String> scoreList = Arrays.asList(str.split(","));
//            listInt.add(addGameNr(scoreList.get(0)));
//            if(scoreList.get(0).equals("45")) {
//                trainList.add(listInt);
//                listInt = new ArrayList<>();
//            }
//
//        }
    }

    private String addGameNr(String str) {
        if(!isNumeric(str)) {
            System.out.println(str);
        }
        return str;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }



}
