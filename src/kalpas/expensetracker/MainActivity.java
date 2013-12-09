package kalpas.expensetracker;

import java.util.List;

import kalpas.expensetracker.EditTranDetailsDialog.NoticeDialogListener;
import kalpas.expensetracker.core.Core;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.TransactionListAdapter;
import kalpas.testservice.R;
import android.app.Activity;
import android.app.DialogFragment;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements NoticeDialogListener, OnItemClickListener {

    public static final String     TAG              = "kalpas.testservice";
    public static final String     KEY_PREFS_SENDER = "pref_sender";

    private Core                   core;

    private TextView               textView;
    private ListView               listView;
    private TransactionListAdapter adapter;
    private List<Transaction>      transactionListSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_alternative);

        // init prefs
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // register receiver
        registerReceiver(receiver, new IntentFilter(BackgroundService.CHANNEL));

        // instantiate core
        core = new Core(getApplicationContext());
        textView = (TextView) findViewById(R.id.TextViewMain);
        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(MainActivity.this);

        // start background service
        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        textView.setText(core.getAccountSummary(this));
        transactionListSource = core.getTransactions(this);
        adapter = new TransactionListAdapter(this, R.layout.list_item, transactionListSource);
        listView.setAdapter(adapter);

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
        case R.id.action_refresh:
            refresh();
            return true;
        case R.id.action_add:
            //TODO
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void refresh(View view) {
        refresh();
    }

    private void refresh() {
        textView.setText(core.getAccountSummary(this));
        adapter.clear();
        adapter.addAll(core.getTransactions(this));
        adapter.notifyDataSetChanged();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
                                           @Override
                                           public void onReceive(Context context, Intent intent) {
                                               refresh(null);
                                           }
                                       };

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Transaction tx = ((EditTranDetailsDialog) dialog).transaction;
        core.updateTransactionDetails(tx, this);
        refresh();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Toast.makeText(this, parent.toString() + " " + v.toString() + " " + position + " " + id, Toast.LENGTH_SHORT)
                .show();

    }

}
