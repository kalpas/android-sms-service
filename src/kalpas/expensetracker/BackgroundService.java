package kalpas.expensetracker;

import kalpas.expensetracker.core.Core;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.transaction.edit.EditTransactionActivity;
import kalpas.sms.parse.PumbSmsParser;
import kalpas.sms.parse.PumbTransaction;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service {

    public static final String CHANNEL           = BackgroundService.class.getSimpleName() + ".broadcast";

    public static final String EXTRA_MESSAGE     = "kalpas.BackgroundService.EXTRA_MESSAGE";
    public static final String EXTRA_TRANSACTION = "kalpas.BackgroundService.EXTRA_TRANSACTION";
    public static final String ACTION_PARSE        = "kalpas.BackgroundService.ACTION_PARSE";
    public static final String ACTION_UPDATE     = "kalpas.BackgroundService.ACTION_UPDATE";
    public static final String ACTION_ADD     = "kalpas.BackgroundService.ACTION_ADD";
    public static final String ACTION_REMOVE = "kalpas.BackgroundService.ACTION_REMOVE";


    private StorageWriter      storage           = new StorageWriter();

    private PumbSmsParser      pumb              = new PumbSmsParser();

    private Core               core;

    @Override
    public void onCreate() {
        super.onCreate();
        core = new Core(getApplicationContext());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        core = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent != null ? intent.getAction() : null;

        if (action != null) {
            if (ACTION_PARSE.equals(action)) {
                String msgBody = null;
                if (intent.hasExtra(EXTRA_MESSAGE)) {
                    msgBody = intent.getStringExtra(EXTRA_MESSAGE);
                    if (storage.isAvailable()) {
                        storage.appendText(getApplicationContext(), "#" + msgBody + "\n");
                        PumbTransaction pumbTx = pumb.parsePumbSms(msgBody);
                        Transaction tx = core.processTransaction(pumbTx);
                        sendEdit(tx);
                    } else {
                        Toast.makeText(getApplicationContext(), "storage not available", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (ACTION_UPDATE.equals(action)) {
                Transaction trx = (Transaction) intent.getSerializableExtra(EXTRA_TRANSACTION);
                core.updateTransactionDetails(trx);
            }else if(ACTION_ADD.equals(action)){
                Transaction trx = (Transaction) intent.getSerializableExtra(EXTRA_TRANSACTION);
                core.addTransaction(trx);
            }else if(ACTION_REMOVE.equals(action)){
                Transaction trx = (Transaction) intent.getSerializableExtra(EXTRA_TRANSACTION);
                core.removeTransaction(trx);
            }
            else {
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

}
