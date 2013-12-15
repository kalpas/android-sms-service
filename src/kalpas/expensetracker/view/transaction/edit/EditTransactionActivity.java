package kalpas.expensetracker.view.transaction.edit;

import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class EditTransactionActivity extends Activity {

    public static final String ACTION_EDIT = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_EDIT";

    private EditText           amount;
    private EditText           description;
    private EditText           tags;
    private TextView           recipient;
    private TextView           date;

    private Transaction        transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);
    }

    @Override
    protected void onStart() {
        super.onStart();

        amount = (EditText) findViewById(R.id.amount);
        description = (EditText) findViewById(R.id.description);
        tags = (EditText) findViewById(R.id.tags);
        recipient = (TextView) findViewById(R.id.recipient);
        date = (TextView) findViewById(R.id.date);

        Intent intent = getIntent();
        String action = intent == null ? null : intent.getAction();

        if (ACTION_EDIT.equals(action)) {
            transaction = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);

            recipient.setText(transaction.recipient);
            amount.setText(transaction.amount.toString());
            description.setText(transaction.description);
            tags.setText(transaction.tags);
            date.setText(transaction.date);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        transaction = null;
    }

    public void discard(View view) {
        this.finish();
    }

    public void update(View view) {
        if (transaction != null) {
            transaction.amount = Double.valueOf(amount.getText().toString());
            transaction.description = description.getText().toString();
            transaction.tags = tags.getText().toString();

            Intent update = new Intent(this, BackgroundService.class);
            update.setAction(BackgroundService.ACTION_UPDATE);
            update.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
            startService(update);

            this.finish();

        }
    }
}
