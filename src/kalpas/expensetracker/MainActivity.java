package kalpas.expensetracker;

import java.util.List;

import kalpas.expensetracker.core.Core;
import kalpas.expensetracker.core.CoreFactory;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.TransactionListAdapter;
import kalpas.expensetracker.view.summary.SummaryActivity;
import kalpas.expensetracker.view.transaction.edit.EditTransactionActivity;
import kalpas.expensetracker.view.transaction.remove.RemoveTransactionDialog;
import kalpas.expensetracker.view.transaction.remove.RemoveTransactionDialog.RemoveTransactionListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener, OnItemLongClickListener,
        RemoveTransactionListener {

    public static final String     KEY_PREF_SORT    = "pref_sort";
    public static final String     TAG              = "kalpas.expensetracker";
    public static final String     KEY_PREFS_SENDER = "pref_sender";

    private Core                   core;

    private TextView               textView;
    private ListView               listView;
    private TransactionListAdapter trxListAdapter;
    private List<Transaction>      transactionListSource;
    private Spinner                sortType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init prefs
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // register receiver
        registerReceiver(receiver, new IntentFilter(BackgroundService.CHANNEL));

        // instantiate core
        core = CoreFactory.getInstance(this);
        textView = (TextView) findViewById(R.id.TextViewMain);
        sortType = (Spinner) findViewById(R.id.sort);
        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(MainActivity.this);
        listView.setOnItemLongClickListener(MainActivity.this);
    }

    @Override
    protected void onStart() {

        // Intent i = new Intent("android.provider.Telephony.SMS_RECEIVED");
        // List<ResolveInfo> infos =
        // getPackageManager().queryBroadcastReceivers(i, 0);
        // for (ResolveInfo info : infos) {
        // Toast.makeText(this, "Receiver name:" + info.activityInfo.name +
        // "; priority=" + info.priority,
        // Toast.LENGTH_SHORT).show();
        // }

        super.onStart();

        textView.setText(core.getAccountSummary());
        transactionListSource = core.getTransactions();
        trxListAdapter = new TransactionListAdapter(this, R.layout.list_item, transactionListSource);
        String sort = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_PREF_SORT,
                TransactionListAdapter.SORT_TYPE_DATE_DESC);
        trxListAdapter.sort(sort);
        listView.setAdapter(trxListAdapter);

        ArrayAdapter<CharSequence> sortTypesAdpater = ArrayAdapter.createFromResource(this, R.array.spinner_sort_types,
                android.R.layout.simple_spinner_item);
        sortTypesAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortType.setAdapter(sortTypesAdpater);
        sortType.setOnItemSelectedListener(new SpinnerOnItemSelectedListener());
        sortType.setSelection(sortTypesAdpater.getPosition(sort));

        Intent intent = getIntent();
        String action = intent == null ? null : intent.getAction();
        if (action != null) {
            if (ClearAllPreference.CLEAR_ALL_ACTION.equals(action)) {
                core.clearData();
                refresh();
            }else if (BackupDataPreference.BACKUP_DATA_ACTION.equals(action)){
                core.backupData();
                refresh();
            }
        }
        setIntent(null);
    }

    private void editTransaction(Transaction trx) {
        Intent intent = new Intent(this, EditTransactionActivity.class);
        intent.setAction(EditTransactionActivity.ACTION_EDIT);
        intent.putExtra(BackgroundService.EXTRA_TRANSACTION, trx);
        startActivity(intent);
    }

    private void removeTransaction(Transaction trx) {
        Intent service = new Intent(this, BackgroundService.class);
        service.setAction(BackgroundService.ACTION_REMOVE);
        service.putExtra(BackgroundService.EXTRA_TRANSACTION, trx);
        startService(service);
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
        case R.id.action_summary:
            Intent summaryIntent = new Intent(this, SummaryActivity.class);
            startActivity(summaryIntent);
            return true;
        case R.id.action_add:
            Intent addIntent = new Intent(this, EditTransactionActivity.class);
            addIntent.setAction(EditTransactionActivity.ACTION_ADD);
            startActivity(addIntent);
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
        refresh();
    }

    private void refresh() {
        textView.setText(core.getAccountSummary());
        trxListAdapter.clear();
        trxListAdapter.addAll(core.getTransactions());
        trxListAdapter.sort((String) sortType.getSelectedItem());
        trxListAdapter.notifyDataSetChanged();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
                                           @Override
                                           public void onReceive(Context context, Intent intent) {
                                               refresh();
                                           }
                                       };

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        editTransaction(trxListAdapter.getItem(position));

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        RemoveTransactionDialog dialog = new RemoveTransactionDialog();
        dialog.transaction = trxListAdapter.getItem(position);
        dialog.show(getFragmentManager(), "remove_transaction");
        return true;
    }

    @Override
    public void onDialogPositiveClick(RemoveTransactionDialog dialog) {
        if (dialog.transaction != null) {
            removeTransaction(dialog.transaction);
        }
    }

    private class SpinnerOnItemSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String sortType = (String) parent.getItemAtPosition(pos);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString(KEY_PREF_SORT, sortType);
            editor.commit();
            trxListAdapter.sort(sortType);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

    }

}
