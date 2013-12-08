package kalpas.testservice;

import kalpas.testservice.core.Core;
import kalpas.testservice.core.Transaction;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final String TAG              = "kalpas.testservice";
    public static final String KEY_PREFS_SENDER = "pref_sender";
    // public static final String KEY_PREFS_DEFAULT_CARD = "pref_default_card";

    private Core               core;

    private TextView           textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        registerReceiver(receiver, new IntentFilter(BackgroundService.CHANNEL));

        core = new Core(getApplicationContext());

        textView = (TextView) findViewById(R.id.TextViewMain);
        textView.setText(core.getSummary(getApplicationContext()));

        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null) {
            if (ClearAllPreference.CLEAR_ALL_ACTION.equals(action)) {
                core.clearData(this);
                refresh(null);
            } else if (BackgroundService.ACTION_EDIT.equals(action)) {
                Transaction trx = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);
                EditTranDetailsDialog dialog = new EditTranDetailsDialog();
                dialog.transaction = trx;
                dialog.show(getFragmentManager(), "edit");
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            openSettings();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void refresh(View view) {
        textView.setText(core.getSummary(getApplicationContext()));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
                                           @Override
                                           public void onReceive(Context context, Intent intent) {
                                               refresh(null);
                                           }
                                       };

}
