package com.redhat.lightblue.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class Constants {

    public static final String DATE_FORMAT_STR = "yyyyMMdd'T'HH:mm:ss.SSSZ";
    /** Contains the lightblue {@link DateFormat} for each Thread. */
    private static final ThreadLocal<DateFormat> DATE_FORMATS = new ThreadLocal<>();
    /** It is faster to clone than to create new {@link DateFormat} instances.
     * This is the base instance from which others are cloned. */
    private static final DateFormat BASE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STR);

    /**
     * Returns a DateFormat instance using the DATE_FORMAT_STR. Clone of
     * the static internal variable, because SimpleDateFormat is not thread safe
     */
    public static DateFormat getDateFormat() {
        if (DATE_FORMATS.get() == null) {
            DATE_FORMATS.set((DateFormat) BASE_DATE_FORMAT.clone());
        }
        return DATE_FORMATS.get();
    }

    private Constants() {}

}
