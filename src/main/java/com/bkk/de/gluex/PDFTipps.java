package com.bkk.de.gluex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PDFTipps extends PDF {
    public PDFTipps(Redis redis) {
        super(redis);
        coordinatesList.add("445,406,572,567");
//        coordinatesList.add("121,52,652,390");
//        coordinatesList.add("121,38,652,360");
    }

}
