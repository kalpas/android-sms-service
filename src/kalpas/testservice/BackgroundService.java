package kalpas.testservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class BackgroundService extends Service {

    public static final String CHANNEL       = BackgroundService.class.getSimpleName() + ".broadcast";

    public static final String EXTRA_MESSAGE = "kalpas.BackgroundService.MESSAGE";
    
    private StorageWriter storage = new StorageWriter();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String msgBody = null;
            if (intent.hasExtra(EXTRA_MESSAGE)) {
                msgBody = intent.getStringExtra(EXTRA_MESSAGE);
                Toast.makeText(getApplicationContext(),"message: " + msgBody, Toast.LENGTH_SHORT).show();
                if(storage.isAvailable()){
                    storage.appendText(getApplicationContext(), "#"+msgBody+"\n");
                }else{
                    Toast.makeText(getBaseContext(), "storage not available", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return Service.START_STICKY;
    }

    private void sendResult() {
        Intent intent = new Intent(CHANNEL);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
