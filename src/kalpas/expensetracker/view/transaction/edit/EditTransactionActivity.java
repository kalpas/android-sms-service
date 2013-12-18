package kalpas.expensetracker.view.transaction.edit;

import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditTransactionActivity extends Activity {

    private final DateTimeFormatter dateFormat  = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static final String      ACTION_EDIT = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_EDIT";

    private EditText                amount;
    private EditText                description;
    private EditText                tags;
    private TextView                recipient;
    private TextView                date;
    private LinearLayout            parent;

    private Transaction             transaction;

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

        Intent intent = getIntent();
        String action = intent == null ? null : intent.getAction();

        if (ACTION_EDIT.equals(action)) {
            transaction = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);

            if (StringUtils.isEmpty(transaction.recipient)) {
                parent.removeView(recipient);
            } else {
                recipient.setText(transaction.recipient);
            }
            amount.setText(transaction.amount.toString());
            description.setText(transaction.description);
            tags.setText(transaction.tags);
            DateTime time = new DateTime(transaction.date);
            date.setText(dateFormat.print(time));

        }

        setIntent(null);
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

        }

        this.finish();
    }
}
