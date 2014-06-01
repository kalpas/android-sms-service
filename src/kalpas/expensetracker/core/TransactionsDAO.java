package kalpas.expensetracker.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import kalpas.expensetracker.MainActivity;
import kalpas.expensetracker.core.Transaction.TranType;
import kalpas.expensetracker.view.utils.DateTimeFormatHolder.DateTimeTypeConverter;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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

    private static final String SDCARD_DIR             = "/smslog";
    private static final String TRANSACTIONS_FILE_NAME = "transactions.json";
    private final String        fileName;
    private final boolean       SDCard;
    private GsonBuilder         gsonBuilder;
    private Gson                gson;

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

    public TransactionsDAO(boolean SDCard, String fileName) {
        if (Strings.isNullOrEmpty(fileName)) {
            this.fileName = TRANSACTIONS_FILE_NAME;
        } else {
            this.fileName = fileName;
        }
        this.SDCard = SDCard;
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter());
        gsonBuilder.registerTypeAdapter(Instant.class, new InstantTypeConverter());
        gson = gsonBuilder.create();

    }

    public TransactionsDAO() {
        this(false, null);
    }

    public void save(List<Transaction> transactions, Context context) {
        FileOutputStream fos = null;
        try {
            if (!SDCard) {
                fos = context.openFileOutput(getFileName(), Context.MODE_PRIVATE);
            } else {
                File dir = new File(Environment.getExternalStorageDirectory().getPath() + SDCARD_DIR);
                dir.mkdirs();
                File file = new File(dir, getFileName());
                Log.d(MainActivity.TAG, file.getAbsolutePath());
                fos = new FileOutputStream(file, false);
            }

            fos.write(gson.toJson(transactions.toArray(new Transaction[transactions.size()])).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Transaction> load(Context context) {
        List<Transaction> transactions = null;
        FileInputStream fis;
        try {
            if (!SDCard) {
                fis = context.openFileInput(getFileName());
            } else {
                File dir = new File(Environment.getExternalStorageDirectory().getPath() + SDCARD_DIR);
                File file = new File(dir, getFileName());
                Log.d(MainActivity.TAG, file.getAbsolutePath());
                fis = new FileInputStream(file);
            }
            Transaction[] fromJson = gson.fromJson(new InputStreamReader(fis), Transaction[].class);
            if (fromJson != null) {
                transactions = Lists.newArrayList(fromJson);
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        ONE_TIME_DATA_CONVERSION(transactions, context);

        return transactions;
    }

    @Deprecated
    // should be removed after v4
    private void ONE_TIME_DATA_CONVERSION(List<Transaction> transactions, Context context) {
        if (transactions == null) {
            return;
        }

        for (Transaction tran : transactions) {
            if (tran.type == null) {
                if (tran.amount > 0) {
                    tran.type = TranType.INCOME;
                } else {
                    tran.type = TranType.EXPENSE;
                }
            }
            tran.amount = Math.abs(tran.amount);
        }
        save(transactions, context);
    }

    public void deleteAll(Context context) {
        context.deleteFile(getFileName());
    }

    private String getFileName() {
        return fileName;
    }
}
