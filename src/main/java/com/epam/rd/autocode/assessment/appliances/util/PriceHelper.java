package com.epam.rd.autocode.assessment.appliances.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Component("priceHelper")
public class PriceHelper {
    private static final BigDecimal UAH_RATE = new BigDecimal("40");

    public String format(BigDecimal price, Locale locale) {
        if (price == null) return "—";
        if ("uk".equals(locale.getLanguage())) {
            BigDecimal uah = price.multiply(UAH_RATE).setScale(0, RoundingMode.HALF_UP);
            return "₴" + uah.toPlainString();
        }
        return "$" + price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
