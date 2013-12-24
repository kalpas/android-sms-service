package kalpas.expensetracker.view.transaction.add;

import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.transaction.edit.EditTransactionActivity;

import org.joda.time.DateTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class AddTransactionActivity extends Activity {

    public static final String ACTION_SPLIT = "kalpas.expensetracker.view.transaction.add.AddTransactionActivity.ACTION_SPLIT";

    private EditText           amount;
    private EditText           description;
    private EditText           tags;
    private ToggleButton       sign;

    private String             action;
    private Transaction        originalTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
    }

    @Override
    protected void onStart() {
        super.onStart();

        amount = (EditText) findViewById(R.id.amount);
        description = (EditText) findViewById(R.id.description);
        tags = (EditText) findViewById(R.id.tags);
        sign = (ToggleButton) findViewById(R.id.sign);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        action = intent == null ? null : intent.getAction();

        if (action != null && ACTION_SPLIT.equals(action)) {
            originalTransaction = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);

            amount.setText(originalTransaction.amount.toString());
            description.setText(originalTransaction.description);
            tags.setText(originalTransaction.tags);
        }

        setIntent(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        action = null;
        originalTransaction = null;
    }

    public void discard(View view) {
        this.finish();
    }

    public void update(View view) {
        Transaction transaction;

        transaction = (action != null && ACTION_SPLIT.equals(action)) ? new Transaction(new DateTime(
                originalTransaction.date)) : new Transaction(new DateTime());

        transaction.amount = Double.valueOf(amount.getText().toString());
        if (!sign.isChecked() && transaction.amount > 0) {
            transaction.amount = -transaction.amount;
        }
        transaction.description = description.getText().toString();
        transaction.tags = tags.getText().toString();

        Intent update = new Intent(this, BackgroundService.class);
        update.setAction(BackgroundService.ACTION_ADD);
        update.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        startService(update);

        if (action != null && ACTION_SPLIT.equals(action)) {
            transaction.recipient = originalTransaction.recipient;
            transaction.date = originalTransaction.date;

            originalTransaction.amount -= transaction.amount;

            Intent intent = new Intent(this, EditTransactionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.setAction(EditTransactionActivity.ACTION_SAVE_SPLIT);
            intent.putExtra(BackgroundService.EXTRA_TRANSACTION, originalTransaction);
            startActivity(intent);
        }

        this.finish();
    }
}
