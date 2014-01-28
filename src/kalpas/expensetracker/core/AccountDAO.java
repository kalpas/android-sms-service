package kalpas.expensetracker.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.content.Context;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class AccountDAO {

    private static final String ACCOUNT_FILE_NAME = "account.json";
    private Gson                gson              = new Gson();

    public void save(List<Account> accounts, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(ACCOUNT_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(gson.toJson(accounts.toArray(new Account[accounts.size()])).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Account> load(Context context) {
        List<Account> accounts = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(ACCOUNT_FILE_NAME);
            accounts = Lists.newArrayList(gson.fromJson(new InputStreamReader(fis), Account[].class));
        } catch (Exception e) {
            e.printStackTrace();
            deleteAll(context);// FIXME just for 1 time
                               // cleanup
        }
        return accounts;
    }

    public void deleteAll(Context context) {
        context.deleteFile(ACCOUNT_FILE_NAME);
    }
}
