package kalpas.testservice.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class PersistentTransactions {

    private static final String TRANSACTIONS_FILE_NAME = "transactions.json";
    private Gson                gson                   = new Gson();
    private String              cardId;

    private List<Transaction>   transactions           = new ArrayList<Transaction>();

    @SuppressWarnings("unused")
    private PersistentTransactions() {
    }

    public PersistentTransactions(String cardId) {
        this.cardId = cardId;
    }

    public void persist(Context context){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(getFileName(), Context.MODE_PRIVATE);
            fos.write(gson.toJson((Transaction[]) transactions.toArray()).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(Context context) {
        FileInputStream fis;
        try {
            fis = context.openFileInput(getFileName());
            transactions = Lists.newArrayList(gson.fromJson(new InputStreamReader(fis), Transaction[].class));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getFileName() {
        return cardId + TRANSACTIONS_FILE_NAME;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
