package kalpas.expensetracker;

import kalpas.expensetracker.core.Core;
import kalpas.expensetracker.core.CoreFactory;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.transaction.edit.EditTransactionActivity;
import kalpas.sms.parse.PumbSmsParser;
import kalpas.sms.parse.PumbSmsParserFactory;
import kalpas.sms.parse.PumbTransaction;
import kalpas.sms.parse.PumbSmsParserFactory.SmsLocale;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service implements OnSharedPreferenceChangeListener {

    private static final String NONE                = "NONE";

    public static final String  KEY_PREF_SMS_LOCALE = "pref_sms_locale";

    public static final String  CHANNEL             = BackgroundService.class.getSimpleName() + ".broadcast";

    public static final String  EXTRA_MESSAGE       = "kalpas.BackgroundService.EXTRA_MESSAGE";
    public static final String  EXTRA_TRANSACTION   = "kalpas.BackgroundService.EXTRA_TRANSACTION";
    public static final String  ACTION_PARSE        = "kalpas.BackgroundService.ACTION_PARSE";
    public static final String  ACTION_UPDATE       = "kalpas.BackgroundService.ACTION_UPDATE";
    public static final String  ACTION_ADD          = "kalpas.BackgroundService.ACTION_ADD";
    public static final String  ACTION_REMOVE       = "kalpas.BackgroundService.ACTION_REMOVE";

    private StorageWriter       storage             = new StorageWriter();

    private PumbSmsParser       parser;

    private Core                core;

    @Override
    public void onCreate() {
        super.onCreate();
        core = CoreFactory.getInstance(this);

    }

    @Override
    public void onDestroy() {
        core = null;
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        String action = intent != null ? intent.getAction() : null;

        if (action != null) {
            if (ACTION_PARSE.equals(action)) {
                String msgBody = null;
                if (intent.hasExtra(EXTRA_MESSAGE)) {
                    msgBody = intent.getStringExtra(EXTRA_MESSAGE);
                    if (storage.isAvailable()) {
                        storage.appendText(this, "#" + msgBody + "\n");
                        PumbTransaction pumbTx = parseMsg(msgBody);
                        Transaction tx = core.processTransaction(pumbTx);
                        if (tx != null) {
                            sendEdit(tx);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "storage not available", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (ACTION_UPDATE.equals(action)) {
                Transaction trx = (Transaction) intent.getSerializableExtra(EXTRA_TRANSACTION);
                core.updateTransactionDetails(trx);
            } else if (ACTION_ADD.equals(action)) {
                Transaction trx = (Transaction) intent.getSerializableExtra(EXTRA_TRANSACTION);
                core.addTransaction(trx);
            } else if (ACTION_REMOVE.equals(action)) {
                Transaction trx = (Transaction) intent.getSerializableExtra(EXTRA_TRANSACTION);
                core.removeTransaction(trx);
            } else {
                Log.e(getClass().toString(), "no such action");
            }
            sendRefresh();
        }
        return Service.START_STICKY;
    }

    private void sendRefresh() {
        Intent intent = new Intent(CHANNEL);
        sendBroadcast(intent);

    }

    private void sendEdit(Transaction transaction) {

        Intent intent = new Intent(getApplicationContext(), EditTransactionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(EditTransactionActivity.ACTION_EDIT);
        intent.putExtra(EXTRA_TRANSACTION, transaction);
        startActivity(intent);

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private PumbTransaction parseMsg(String msgBody) {
        PumbTransaction trx = null;
        if (parser == null) {
            String value = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_PREF_SMS_LOCALE, NONE);
            if (!NONE.equals(value)) {
                parser = PumbSmsParserFactory.getInstance(SmsLocale.valueOf(value));
            } else {
                parser = getSuitableParser(msgBody);
                if (parser == null) {
                    return trx;
                }
            }
        }
        try {
            trx = parser.parsePumbSms(msgBody);
        } catch (IllegalArgumentException e) {
            failGracefully(msgBody);
        }
        return trx;
    }

    private PumbSmsParser getSuitableParser(String msgBody) {
        PumbSmsParser guess;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        SmsLocale locale = SmsLocale.UA;
        guess = PumbSmsParserFactory.getInstance(locale);
        try {
            guess.parsePumbSms(msgBody);
            editor.putString(KEY_PREF_SMS_LOCALE, locale.toString());
        } catch (IllegalArgumentException e) {
            locale = SmsLocale.EN;
            guess = PumbSmsParserFactory.getInstance(locale);
            try {
                guess.parsePumbSms(msgBody);
                editor.putString(KEY_PREF_SMS_LOCALE, locale.toString());
            } catch (IllegalArgumentException e2) {
                failGracefully(msgBody);
                guess = null;
            }
        }
        editor.commit();
        return guess;

    }

    private void failGracefully(String msgBody) {
        storage.appendText(this, "not able to parse this sms format\n");
        Toast.makeText(this, "wasn't able to parse following msg: " + msgBody + "\nplease conrtact developer",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_PREF_SMS_LOCALE.equals(key)) {
            String value = sharedPreferences.getString(KEY_PREF_SMS_LOCALE, NONE);
            if (!NONE.equals(value)) {
                parser = PumbSmsParserFactory.getInstance(SmsLocale.valueOf(value));
            }
        }

    }
}
