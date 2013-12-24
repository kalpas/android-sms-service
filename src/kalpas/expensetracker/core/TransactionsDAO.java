package kalpas.expensetracker.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import android.content.Context;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TransactionsDAO {

    private static final String TRANSACTIONS_FILE_NAME = "transactions.json";
    private GsonBuilder         gsonBuilder;
    private Gson                gson;

    private static class DateTimeTypeConverter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
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

    private static class InstantTypeConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant src, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(src.getMillis());
        }

        @Override
        public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            return new Instant(json.getAsLong());
        }
    }

    public TransactionsDAO() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter());
        gsonBuilder.registerTypeAdapter(Instant.class, new InstantTypeConverter());
        gson= gsonBuilder.create();
    }

    public void save(Set<Transaction> transactions, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(getFileName(), Context.MODE_PRIVATE);
            fos.write(gson.toJson(transactions.toArray(new Transaction[transactions.size()])).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Transaction> load(Context context) {
        Set<Transaction> transactions = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(getFileName());
            Transaction[] fromJson = gson.fromJson(new InputStreamReader(fis), Transaction[].class);
            if (fromJson != null) {
                transactions = Sets.newHashSet(fromJson);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public void deleteAll(Context context) {
        context.deleteFile(getFileName());
    }

    private String getFileName() {
        return TRANSACTIONS_FILE_NAME;
    }
}
