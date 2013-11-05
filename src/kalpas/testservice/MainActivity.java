package kalpas.testservice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.hello);

        registerReceiver(receiver, new IntentFilter(BackgroundService.CHANNEL));

        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
           @Override
           public void onReceive(Context context,Intent intent) {
               textView.setText("Message from Service");
           }
    };

}
