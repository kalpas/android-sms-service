package kalpas.expensetracker.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

import com.google.gson.Gson;

public class AccountDAO {

    private static final String ACCOUNT_FILE_NAME = "account.json";
    private Gson                gson            = new Gson();


    public void save(Account account, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(getFileName(account.id), Context.MODE_PRIVATE);
            fos.write(gson.toJson(account).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Account load(String accountId, Context context){
        Account account = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(getFileName(accountId));
            account = gson.fromJson(new InputStreamReader(fis), Account.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return account;
    }
    
    public void delete(String accountId, Context context){
        context.deleteFile(getFileName(accountId));
    }


    private String getFileName(String accountId) {
        return accountId + ACCOUNT_FILE_NAME;
    }

}
