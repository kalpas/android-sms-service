package kalpas.expensetracker.view.transaction.add;

import static kalpas.expensetracker.view.utils.DateTimeUtil.dateFormatMid;
import static kalpas.expensetracker.view.utils.DateTimeUtil.timeFormatMid;
import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.datetime.DatePickerFragment;
import kalpas.expensetracker.view.datetime.TimePickerFragment;
import kalpas.expensetracker.view.transaction.edit.EditTransactionActivity;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AddTransactionActivity extends Activity implements TimePickerFragment.TimeSetListener,
        DatePickerFragment.DateSetListener {

    

    public static final String             ACTION_SPLIT = "kalpas.expensetracker.view.transaction.add.AddTransactionActivity.ACTION_SPLIT";

    private TextView                       date;
    private TextView                       time;
    private EditText                       amount;
    private EditText                       description;
    private EditText                       tags;
    private ToggleButton                   sign;

    private DateTime                       dateTime;

    private String                         action;
    private Transaction                    originalTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
    }

    @Override
    protected void onStart() {
        super.onStart();

        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
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
            dateTime = originalTransaction.date;
        } else {
            dateTime = new DateTime();
        }

        updateDateTime();
    }

    @Override
    protected void onDestroy() {
        action = null;
        originalTransaction = null;
        setIntent(null);
        super.onDestroy();
    }

    public void discard(View view) {
        this.finish();
    }

    public void update(View view) {
        Transaction transaction;

        transaction = new Transaction(dateTime);

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

    private void updateDateTime() {
        date.setText(dateFormatMid.print(dateTime));
        time.setText(timeFormatMid.print(dateTime));
    }

    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        MutableDateTime newTime = dateTime.toMutableDateTime();
        newTime.setHourOfDay(hourOfDay);
        newTime.setMinuteOfHour(minute);
        dateTime = newTime.toDateTime();
        updateDateTime();
    }

    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.time = dateTime;
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        MutableDateTime newDate = dateTime.toMutableDateTime();
        newDate.setYear(year);
        newDate.setMonthOfYear(month + 1);
        newDate.setDayOfMonth(day);
        dateTime = newDate.toDateTime();
        updateDateTime();
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.date = dateTime;
        newFragment.show(getFragmentManager(), "datePicker");
    }

}
