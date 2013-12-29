package kalpas.expensetracker.view.transaction.edit;

import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.transaction.add.AddTransactionActivity;
import kalpas.expensetracker.view.utils.DateTimeUtil;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditTransactionActivity extends Activity {

    public static final String ACTION_EDIT       = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_EDIT";
    public static final String ACTION_SAVE_SPLIT = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_SAVE_SPLIT";

    private EditText           amount;
    private EditText           description;
    private EditText           tags;
    private TextView           recipient;
    private TextView           date;
    private LinearLayout       parent;

    private Transaction        transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);
    }

    @Override
    protected void onStart() {
        super.onStart();

        parent = (LinearLayout) findViewById(R.id.parent);

        amount = (EditText) findViewById(R.id.amount);
        description = (EditText) findViewById(R.id.description);
        tags = (EditText) findViewById(R.id.tags);
        recipient = (TextView) findViewById(R.id.recipient);
        date = (TextView) findViewById(R.id.date);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent == null ? null : intent.getAction();

        if (ACTION_EDIT.equals(action) || ACTION_SAVE_SPLIT.equals(action)) {

            transaction = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);

            if (ACTION_SAVE_SPLIT.equals(action)) {
                sendUpdate();
            }

            if (StringUtils.isEmpty(transaction.recipient)) {
                parent.removeView(recipient);
            } else {
                recipient.setText(transaction.recipient);
            }
            amount.setText(transaction.amount.toString());
            description.setText(transaction.description);
            tags.setText(transaction.tags);
            DateTime time = new DateTime(transaction.date);
            date.setText(DateTimeUtil.toString(time));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        setIntent(null);
        transaction = null;
        super.onDestroy();
    }

    public void discard(View view) {
        this.finish();
    }

    public void update(View view) {
        update();
        this.finish();
    }

    private void update() {
        updateWithChanges();
        sendUpdate();
    }

    private void sendUpdate() {
        Intent update = new Intent(this, BackgroundService.class);
        update.setAction(BackgroundService.ACTION_UPDATE);
        update.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        startService(update);
    }

    private void updateWithChanges() {
        transaction.amount = Double.valueOf(amount.getText().toString());
        transaction.description = description.getText().toString();
        transaction.tags = tags.getText().toString();
    }

    public void split(View view) {
        updateWithChanges();
        sendSplit();
    }

    private void sendSplit() {
        Intent splitIntent = new Intent(this, AddTransactionActivity.class);
        splitIntent.setAction(AddTransactionActivity.ACTION_SPLIT);
        splitIntent.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        startActivity(splitIntent);
    }
}
