package com.bkk.de.gluex;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Excel {

    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.YYYY", Locale.GERMAN);
    SimpleDateFormat formatter2 = new SimpleDateFormat("dd.M.yyyy", Locale.ENGLISH);
    SimpleDateFormat formatYear = new SimpleDateFormat("YYYY");
    Redis redis;

    public Excel(Redis redis) {
        this.redis = redis;
    }

    public void read(String fileLocation) throws Exception {
        FileInputStream file = new FileInputStream(new File(fileLocation));
        Workbook workbook = new XSSFWorkbook(file);
        Iterator<Sheet> sheetIterator;
        sheetIterator = workbook.sheetIterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            String year = "";
            for (Row row : sheet) {
                List<String> resultList = new ArrayList<>();
                String key = "";
                if (row.getCell(1) != null && row.getCell(1).getCellType().equals(CellType.NUMERIC) && (int) row.getCell(1).getNumericCellValue() >= 1975 && (int) row.getCell(1).getNumericCellValue() <= 2030) {
                    year = String.valueOf((int) row.getCell(1).getNumericCellValue());
                }
                if (row.getCell(0) != null && row.getCell(0).getCellType().equals(CellType.NUMERIC) && row.getCell(0).getNumericCellValue() >= 1 && row.getCell(0).getNumericCellValue() <= 53) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
//                            System.out.println(cell.getRichStringCellValue().getString());
                                resultList.add(cell.getRichStringCellValue().getString());
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    resultList.add(formatter.format(cell.getDateCellValue()));
                                } else {
                                    resultList.add(String.valueOf((int) cell.getNumericCellValue()));
                                }
                                break;
                            default:
//                                System.out.println(cell.getCellType());
                        }
                    }
                    if (resultList.size() < 10) {
                        throw new Exception("ERROR; resultlist too short!!");
                    }
                    key = "VA-" + year + "-" + (int) row.getCell(0).getNumericCellValue();
                    System.out.println(key + " <> " + String.join(",", resultList));
                    redis.write(key, String.join(",", resultList));
                }
            }
        }
//        System.out.println(String.join(",",resultList));
    }

    public void readNewer(String fileLocation) throws Exception {
        FileInputStream file = new FileInputStream(new File(fileLocation));
        Workbook workbook = new XSSFWorkbook(file);
        Iterator<Sheet> sheetIterator;
        sheetIterator = workbook.sheetIterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            String year = "";
            int counter = 1;
            for (Row row : sheet) {
                List<String> resultList = new ArrayList<>();
                String key = "";
                if (row.getCell(0) != null && isValidDate(row.getCell(0).getStringCellValue())) {
                    if (year.isEmpty()) {
                        year = formatYear.format(formatter2.parse(row.getCell(0).getStringCellValue()));
                    }
                    if (Integer.valueOf(year) < 2022) {
                        continue;
                    }
                    key = "VA-" + year + "-" + counter;
                    resultList.add(String.valueOf(counter++));
                    int ccounter = 0;
                    for (Cell cell : row) {
                        ccounter++;
                        if (ccounter == 9 || ccounter == 10) {
                            continue;
                        }
                        switch (cell.getCellType()) {
                            case STRING:
                                String strValue = cell.getRichStringCellValue().getString().trim();
                                if (strValue.contains(",") || strValue.chars().filter(ch -> ch == '.').count() == 1) {
                                    resultList.add(String.valueOf(NumberFormat.getNumberInstance(Locale.GERMAN).parse(cell.getRichStringCellValue().getString()).intValue()));
                                } else {
                                    resultList.add(cell.getRichStringCellValue().getString().trim().replace("Jackpot","JP"));
                                }
                                break;
                            case NUMERIC:
                                resultList.add(String.valueOf((int) cell.getNumericCellValue()).trim());
                                break;
                            default:
                                throw new Exception("ERROR; wrong Type!!!");
                        }
                    }
                    if (resultList.size() < 10) {
                        throw new Exception("ERROR; resultlist too short!!");
                    }

                    System.out.println(key + " <> " + String.join(",", resultList));
                    redis.write(key, String.join(",", resultList));
                }
            }
        }
//        System.out.println(String.join(",",resultList));
    }

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.M.YYYY");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

}
