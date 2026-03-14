package com.badminton.booking.common.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtil {

    private CurrencyUtil() {
    }

    public static String formatVnd(BigDecimal amount) {
        if (amount == null) {
            return "0 VNĐ";
        }

        NumberFormat format = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return format.format(amount) + " VNĐ";
    }
}