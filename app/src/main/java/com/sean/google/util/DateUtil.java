package com.sean.google.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String getTodayYMDStry() {
        return getDateStr(new Date(), null);
    }

    public static String getYMDStr(Date date) {
        return getDateStr(date, null);
    }

    public static String getNow() {
        return getDateStr(new Date(), "yyyy-MM-dd_HH_mm_ss");
    }

    public static String getDateStr(Date date, String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }
}
