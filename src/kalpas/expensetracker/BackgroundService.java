package kalpas.expensetracker;

import kalpas.expensetracker.core.Core;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.transaction.edit.EditTransactionActivity;
import kalpas.sms.parse.PumbSmsParser;
import kalpas.sms.parse.PumbTransaction;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class BackgroundService extends Service {

    public static final String CHANNEL           = BackgroundService.class.getSimpleName() + ".broadcast";

    public static final String EXTRA_MESSAGE     = "kalpas.BackgroundService.MESSAGE";
    public static final String EXTRA_TRANSACTION = "kalpas.BackgroundService.TRANSACTION";
    public static final String ACTION_EDIT       = "kalpas.BackgroundService.EDIT";

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

        if (intent != null) {
            String msgBody = null;
            if (intent.hasExtra(EXTRA_MESSAGE)) {
                msgBody = intent.getStringExtra(EXTRA_MESSAGE);
                if (storage.isAvailable()) {
                    storage.appendText(getApplicationContext(), "#" + msgBody + "\n");
                    PumbTransaction pumbTx = pumb.parsePumbSms(msgBody);
                    Transaction tx = core.processTransaction(pumbTx, getApplicationContext());

                    Intent intent2 = new Intent(getApplicationContext(), EditTransactionActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent2);
                    // sendEdit(tx);
                } else {
                    Toast.makeText(getApplicationContext(), "storage not available", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return Service.START_STICKY;
    }

    private void sendRefresh() {
        Intent intent = new Intent(CHANNEL);
        sendBroadcast(intent);

    }

    private void sendEdit(Transaction transaction) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(ACTION_EDIT);
        intent.putExtra(EXTRA_TRANSACTION, transaction);
        getApplicationContext().startActivity(intent);

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
