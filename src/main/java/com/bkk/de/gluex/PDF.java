package com.bkk.de.gluex;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PDF {

    Splitter splitter;
    List<String> coordinatesList;
    String stringScores;
    Redis redis;

    @Autowired
    public PDF(Redis redis) {
        this.redis = redis;
        this.splitter = new Splitter();
        coordinatesList = new ArrayList<>();
        coordinatesList.add("121,59,652,390");
        coordinatesList.add("121,51,652,395");
        coordinatesList.add("121,52,652,390");
        coordinatesList.add("121,38,652,365");
    }

    private void deleteTmpFile() {
        File myObj = new File("/tmp/gluex.pdf");
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
        } else {
            System.out.println("Failed to delete the file." + myObj.getName());
        }
    }

    public void scanGluexPDF(Path documentPath) {
        split(documentPath);
    }

    public void split(Path documentPath) {
        try {
            deleteTmpFile();
            PDDocument document = PDDocument.load(documentPath.toFile());
            int loops = document.getNumberOfPages() < 10 ? document.getNumberOfPages() - 1 : 10;
            for (int i = loops; i > 1; i--) {
                PDDocument page = new PDDocument();
                page.addPage(document.getPage(i));
                page.save(new File("/tmp/gluex.pdf"));
                if (checkStringAuswahlwette(page)) {
                    page.save(new File("/tmp/gluex.pdf"));
                    page.save(new File("/home/bkk/gluex/side10/" + documentPath.getFileName()));
//                    runThroughCoordinatesList(documentPath.getFileName());
                    return;
                }
            }
            throw new Exception("ERROR cannot find score page!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkStringAuswahlwette(PDDocument page) {
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            List<String> items = Arrays.asList(pdfStripper.getText(page).split("\n"));
            for (String str : items) {
                if (str.startsWith("13er-Wette / 6aus45 Auswahlwette")
                        || str.startsWith("13er Ergebnistipp / 6aus45 Auswahltipp")
                        || str.startsWith("13er Ergebniswette / 6aus45 Auswahlwette")
                        || str.startsWith("6aus45 Auswahlwette 13er Ergebniswettrunde")
                        || str.startsWith("6aus45 Auswahlwette 13:00 Uhr/13er Ergebniswette 15:00 Uhr")
                        || str.startsWith("6aus45 Auswahlwette 13er Ergebniswette")) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void runThroughCoordinatesList(Path file) {
        try {
            for (String str : coordinatesList) {
                if (extractData(str, file)) {
                    return;
                }
            }
            throw new Exception("ERROR runThroughCoordinatesList -> " + file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private boolean extractData(String coordinates, Path path) {

        try {
            Process p = Runtime.getRuntime().exec("java -jar /home/bkk/bin/tabula-1.0.5-jar-with-dependencies.jar -n -p 1 -r -a " + coordinates + " /tmp/gluex.pdf");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String s;
            while ((s = stdInput.readLine()) != null) {
                stringBuilder.append(s);
                stringBuilder.append("\n");
            }
            if (stringBuilder.toString().contains(",,,,,,,,") || stringBuilder.toString().contains("\n*")) {
                p = Runtime.getRuntime().exec("java -jar /home/bkk/bin/tabula-1.0.5-jar-with-dependencies.jar -n -p 1 -a " + coordinates + " /tmp/gluex.pdf");
                stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                stringBuilder = new StringBuilder();
                s = "";
                while ((s = stdInput.readLine()) != null) {
                    stringBuilder.append(s);
                    stringBuilder.append("\n");
                }
            }
            if (stringBuilder.toString().contains("Ersatzauslosung") || stringBuilder.toString().contains("Frauen-WM")
                    || stringBuilder.toString().contains("Erster Gruppe")
                    || stringBuilder.toString().contains("FA-Cup")
                    || stringBuilder.toString().contains("meisterschaft")
                    || stringBuilder.toString().contains("Deutschland")
                    || stringBuilder.toString().contains("Frankreich")
                    || stringBuilder.toString().contains("Sieger")
                    || stringBuilder.toString().contains("finale")
                    || stringBuilder.toString().contains("Pokal")) {
                return true;
            }

            String strSplit[] = stringBuilder.toString().split(",");
            if (strSplit[0].equals("1") && strSplit[0].length() < 3 && Tools.isNumeric(strSplit[0])) {
//                extractSingleRows(coordinates);
                corrections(stringBuilder);
                stringScores = stringBuilder.toString();
                String newString = stringScores.replace("\n*", "*").replace("\"", "").replace("\nTip", "").replace("\nErg", "").replace("C*\n", "");
                System.out.println(newString);
                redis.write(Tools.getKey("ROW", path.getFileName().toString()), newString);
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void corrections(StringBuilder stringBuilder) {
        replaceAll(stringBuilder, "bnis ohne HC*", "");
        replaceAll(stringBuilder, "bnis mit HC*", "");
        replaceAll(stringBuilder, "bnis mit HC*", "");
        replaceAll(stringBuilder, "bnis mit HC*", "");
        replaceAll(stringBuilder, ",,,,,,,,", "");
        replaceAll(stringBuilder, "HC*", "");
    }

    public void replaceAll(StringBuilder stringBuilder, String from, String to) {
        int index = stringBuilder.indexOf(from);
        if (index > 0) {
            stringBuilder.replace(index, index + from.length(), to);
        }
    }

    private String correctResultLine(String str) {

        Map<String, String> correctionList = new HashMap<>();

        correctionList.put("14,MO,MalmFöo FtFo :( 5p)r i-v Haetlsingborgs IF (16),,7-2-1,-,4:1,-,UsUsNNnS,suNUnNnn",
                "14,MO,Malmö FF (5) - Haetlsingborgs IF (16),,7-2-1,-,4:1,-,UsUsNNnS,suNUnNnn");

        correctionList.put("37,SO,Stade Rennes FC (9) - AS Saint-Etienne (4) 1:1Ergebnis Smsit HC**,,2-4-4 1:1,2:0,sSN*uNs,,,sNUsSsnU",
                "37,SO,Stade Rennes FC (9) - AS Saint-Etienne (4),,2-4-4,-,1:1,2:0,sSN*uNs,,,sNUsSsnU");

        List<String> corrList = Arrays.asList(str);
        correctionList
                .entrySet()
                .stream()
                .forEach(entry -> {
                    String repl = corrList.get(0).replace(entry.getKey(), entry.getValue());
                    corrList.set(0, repl);
                });

        return corrList.get(0);

    }

    private void searchReplace(String search, String replace,
                               String encoding, boolean replaceAll, PDDocument doc) throws IOException {
        PDPageTree pages = doc.getDocumentCatalog().getPages();
        for (PDPage page : pages) {
            PDFStreamParser parser = new PDFStreamParser(page);
            parser.parse();
            List tokens = parser.getTokens();
            for (int j = 0; j < tokens.size(); j++) {
                Object next = tokens.get(j);
                if (next instanceof Operator) {
                    Operator op = (Operator) next;
                    // Tj and TJ are the two operators that display strings in a PDF
                    // Tj takes one operator and that is the string to display so lets update that operator
                    if (op.getName().equals("Tj")) {
                        COSString previous = (COSString) tokens.get(j - 1);
                        String string = previous.getString();
                        if (replaceAll)
                            string = string.replaceAll(search, replace);
                        else
                            string = string.replaceFirst(search, replace);
                        previous.setValue(string.getBytes());
                    } else if (op.getName().equals("TJ")) {
                        COSArray previous = (COSArray) tokens.get(j - 1);
                        for (int k = 0; k < previous.size(); k++) {
                            Object arrElement = previous.getObject(k);
                            if (arrElement instanceof COSString) {
                                COSString cosString = (COSString) arrElement;
                                String string = cosString.getString();
                                if (replaceAll)
                                    string = string.replaceAll(search, replace);
                                else
                                    string = string.replaceFirst(search, replace);
                                cosString.setValue(string.getBytes());
                            }
                        }
                    }
                }
            }
            // now that the tokens are updated we will replace the page content stream.
            PDStream updatedStream = new PDStream(doc);
            OutputStream out = updatedStream.createOutputStream();
            ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
            tokenWriter.writeTokens(tokens);
            out.close();
            page.setContents(updatedStream);
        }
    }

    public void mainReplace() {
        try {
            String outputFileName = "/tmp/gluex.pdf";
            // the encoding will need to be adapted to your circumstances
            String encoding = "ISO-8859-1";

            // Create a document and add a page to it
            PDDocument document = PDDocument.load(new File("/tmp/gluex.pdf"));
//            PDPage page1 = new PDPage(PDRectangle.A4);
//            // PDRectangle.LETTER and others are also possible
//            PDRectangle rect = page1.getMediaBox();
//            // rect can be used to get the page width and height
//            document.addPage(page1);
//
//            // Create a new font object selecting one of the PDF base fonts
//            PDFont fontPlain = PDType1Font.HELVETICA;

            // Start a new content stream which will "hold" the to be created content
//            PDPageContentStream cos = new PDPageContentStream(document, page1);

            // Define a text content stream using the selected font, move the cursor and draw some text
//            cos.beginText();
//            cos.setFont(fontPlain, 12);
//            cos.newLineAtOffset(100, rect.getHeight() - 50);
//            // add 'Hello World' twice
//            cos.showText("Hello World, Hello World");
//            cos.endText();
//
//            // Make sure that the content stream is closed
//            cos.close();

            // Note that search and replace can be regular expressions
            // replace all occurrences of 'Hello'
            searchReplace("Ergebnis mit HC*", "", encoding, true, document);
            searchReplace("Ergebnis ohne HC*", "", encoding, true, document);
            searchReplace("Ergebnis mit HC *", "", encoding, true, document);
            searchReplace("Ergebnis ohne HC *", "", encoding, true, document);
            // replace only first occurrence of 'World'
//        searchReplace("World", "Earth", encoding, false, document);

            // Save the results and ensure that the document is properly closed
            document.save(outputFileName);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void extractSingleRows(String coordinates) {
        try {
            String strSplit[] = coordinates.split(",");

            List<Double> listDouble = new ArrayList<>();
            Arrays.asList(coordinates.split(",")).forEach(s -> listDouble.add(Double.valueOf(s)));
            List<String> listCoordinates = listDouble.stream().map(i -> i.toString()).collect(Collectors.toList());
            listDouble.set(2,listDouble.get(0));
            for(int i=0; i<45; i++) {
                listDouble.set(2,listDouble.get(2)+12.5);

                listCoordinates = listDouble.stream().map(m -> m.toString()).collect(Collectors.toList());
                Process p = Runtime.getRuntime().exec("java -jar /home/bkk/bin/tabula-1.0.5-jar-with-dependencies.jar -n -p 1 -a " + String.join(",",listCoordinates) + " /tmp/gluex.pdf");
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                int s;
                String formattedString = "";
                while ((s = stdInput.read()) != -1) {
                    char character = (char) s;
//                    String newStr = s.replace("\n*","");
//                    stringBuilder.append(s);
//                    stringBuilder.append("\n");
                    formattedString += character;
                }
                String newStr = formattedString.replace("\r*","").replace("\n**","").replace("*,","").replace("**,","");
                System.out.println(newStr);
                listDouble.set(0,listDouble.get(0)+12.5);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
