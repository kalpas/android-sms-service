package kalpas.testservice.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class TransactionsDAO {

    private static final String TRANSACTIONS_FILE_NAME = "transactions.json";
    private Gson                gson                   = new Gson();

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
