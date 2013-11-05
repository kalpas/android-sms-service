package kalpas.testservice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView textView;
    EditText sender;
    
    public static final String TAG = "kalpas.testservice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.hello);
        sender = (EditText) findViewById(R.id.sender);
        

        registerReceiver(receiver, new IntentFilter(BackgroundService.CHANNEL));

        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        sender.setText(Preferences.getSender(this));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
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
    
    public void saveSender(View view){
        EditText text = (EditText) findViewById(R.id.sender);
        String sender = text.getText().toString();
        Preferences.setSender(this, sender);
        
    }

}
