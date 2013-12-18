package kalpas.expensetracker.view.transaction.add;

import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;

import org.joda.time.DateTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class AddTransactionActivity extends Activity {

    private EditText amount;
    private EditText description;
    private EditText tags;
    private ToggleButton   sign;

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

    public void discard(View view) {
        this.finish();
    }

    public void update(View view) {

        Transaction transaction = new Transaction(new DateTime());
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

        this.finish();
    }

}
