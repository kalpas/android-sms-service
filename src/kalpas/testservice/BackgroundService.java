package kalpas.testservice;

import kalpas.sms.parse.PumbSmsParser;
import kalpas.sms.parse.PumbTransaction;
import kalpas.testservice.core.Core;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class BackgroundService extends Service {

    public static final String CHANNEL       = BackgroundService.class.getSimpleName() + ".broadcast";

    public static final String EXTRA_MESSAGE = "kalpas.BackgroundService.MESSAGE";
    
    private StorageWriter storage = new StorageWriter();
    
    private PumbSmsParser pumb = new PumbSmsParser();
    
    private Core core;

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
                if(storage.isAvailable()){
                    storage.appendText(getApplicationContext(), "#"+msgBody+"\n");
                    PumbTransaction tx =  pumb.parsePumbSms(msgBody);
                    core.processTransaction(tx, getApplicationContext());
                    sendRefresh();
                }else{
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

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
