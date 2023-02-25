package com.bkk.de.gluex;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class InitBean implements InitializingBean {

    PDF pdf;
    Neuronal neuronal;

    public InitBean(PDF pdf, Neuronal neuronal) {
        this.pdf = pdf;
        this.neuronal = neuronal;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean#afterPropertiesSet()");
        pdf.getAllFiles("/home/bkk/gluex/pdf")
                .forEach(f -> {
                    System.out.println(f.getFileName());
                    neuronal.insertTrainList(pdf.createScores(f));
                });
    }

}
