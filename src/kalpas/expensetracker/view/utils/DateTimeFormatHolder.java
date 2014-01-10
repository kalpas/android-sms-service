package kalpas.expensetracker.view.utils;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateTimeFormatHolder {

    private final static DateTimeFormatter dateFormat    = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter  dateFormatMid = DateTimeFormat.mediumDate().withLocale(Locale.UK);
    public static final DateTimeFormatter  timeFormatMid = DateTimeFormat.mediumTime().withLocale(Locale.UK);

    public static final DateTimeFormatter  dateTimeMid   = DateTimeFormat.mediumDateTime().withLocale(Locale.UK);

    public static String toString(DateTime date) {
        return dateFormat.print(date);
    }

    public static class DateTimeTypeConverter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
        @Override
        public JsonElement serialize(DateTime src, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return new DateTime(json.getAsString());
            } catch (IllegalArgumentException e) {
                // May be it came in formatted as a java.util.Date, so try that
                Date date = context.deserialize(json, Date.class);
                return new DateTime(date);
            }
        }
    }
}
