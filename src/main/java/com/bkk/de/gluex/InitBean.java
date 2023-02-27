package com.bkk.de.gluex;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InitBean implements InitializingBean {

    PDFTipps pdfTipps;
    Neuronal neuronal;

    Excel excel;

    @Autowired
    public InitBean(PDFTipps pdfTipps, Neuronal neuronal, Excel excel) {
        this.pdfTipps = pdfTipps;
        this.neuronal = neuronal;
        this.excel = excel;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

//        excel.read("/home/bkk/gluex/Auswahlwette1975.xlsx");
        excel.readNewer("/home/bkk/gluex/AW_ab_2018.xlsx");

//        neuronal.getRows();

//        pdfTipps.getAllFiles("/home/bkk/gluex/pdf")
//                .forEach(f -> {
//                    System.out.println(f.getFileName());
//                    pdfTipps.createScores(f);
//                });
    }

}
