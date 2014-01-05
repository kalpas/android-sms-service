package kalpas.expensetracker.view.utils;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeFormatHolder {

    private final static DateTimeFormatter dateFormat    = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter  dateFormatMid = DateTimeFormat.mediumDate().withLocale(Locale.getDefault());
    public static final DateTimeFormatter  timeFormatMid = DateTimeFormat.shortTime().withLocale(Locale.getDefault());

    public static final DateTimeFormatter  dateTimeMid   = DateTimeFormat.mediumDateTime().withLocale(
                                                                 Locale.getDefault());

    public static String toString(DateTime date) {
        return dateFormat.print(date);
    }
}
