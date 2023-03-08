//package com.bkk.de.gluex;
//
//import com.spire.pdf.PdfDocument;
//import com.spire.pdf.utilities.PdfTable;
//import com.spire.pdf.utilities.PdfTableExtractor;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//public class ExtractTableData {
//    public static void main(String []args) {
//
//        //Load a sample PDF document
//        try {
//            PdfDocument pdf = new PdfDocument("/tmp/gluex.pdf");
//
//            //Create a StringBuilder instance
//            StringBuilder builder = new StringBuilder();
//            //Create a PdfTableExtractor instance
//            PdfTableExtractor extractor = new PdfTableExtractor(pdf);
//
//            //Loop through the pages in the PDF
////            for (int pageIndex = 0; pageIndex < pdf.getPages().getCount(); pageIndex++) {
//
//                //Extract tables from the current page into a PdfTable array
//                PdfTable[] tableLists = extractor.extractTable(0);
//
//                //If any tables are found
//                if (tableLists != null && tableLists.length > 0) {
//                    //Loop through the tables in the array
//                    for (PdfTable table : tableLists) {
//                        //Loop through the rows in the current table
//                        for (int i = 0; i < table.getRowCount(); i++) {
//                            //Loop through the columns in the current table
//                            for (int j = 0; j < table.getColumnCount(); j++) {
//                                //Extract data from the current table cell and append to the StringBuilder
//                                String text = table.getText(i, j).replace("*\n", "").replace("*", "");
//                                builder.append(text + " | ");
//                            }
//                            builder.append("\r\n");
//                        }
//                    }
//                    checkLen(builder.toString());
//                }
////            }
//
//            //Write data into a .txt document
//            FileWriter fw = new FileWriter("/home/bkk/ExtractTable.txt");
//            fw.write(builder.toString());
//            fw.flush();
//            fw.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static void checkLen(String str) {
//        Arrays.asList(str.split("\n"))
//                .forEach(s -> {
//                    List<String> list = Arrays.asList(s.split("\\|"));
////                    if(list.size() == 10)
//                        System.out.println(s);
//                });
//    }
//}
