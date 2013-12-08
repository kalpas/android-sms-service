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

    public void save(Set<Transaction> transactions, String cardId, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(getFileName(cardId), Context.MODE_PRIVATE);
            fos.write(gson.toJson(transactions.toArray(new Transaction[transactions.size()])).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Transaction> load(String cardId, Context context) {
        Set<Transaction> transactions = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(getFileName(cardId));
            Transaction[] fromJson = gson.fromJson(new InputStreamReader(fis), Transaction[].class);
            if (fromJson != null) {
                transactions = Sets.newHashSet(fromJson);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public void delete(String cardId, Context context) {
        context.deleteFile(getFileName(cardId));
    }

    private String getFileName(String cardId) {
        return cardId + TRANSACTIONS_FILE_NAME;
    }
}
