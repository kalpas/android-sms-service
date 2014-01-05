package kalpas.expensetracker.view.transaction.edit;

import static kalpas.expensetracker.view.utils.DateTimeUtil.dateFormatMid;
import static kalpas.expensetracker.view.utils.DateTimeUtil.timeFormatMid;
import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.datetime.DatePickerFragment;
import kalpas.expensetracker.view.datetime.TimePickerFragment;
import kalpas.expensetracker.view.transaction.add.AddTransactionActivity;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditTransactionActivity extends Activity implements TimePickerFragment.TimeSetListener,
        DatePickerFragment.DateSetListener {

    public static final String ACTION_EDIT       = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_EDIT";
    public static final String ACTION_SAVE_SPLIT = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_SAVE_SPLIT";

    private TextView           date;
    private TextView           time;

    private EditText           amount;
    private EditText           description;
    private EditText           tags;
    private TextView           recipient;
    private LinearLayout       parent;

    private Transaction        transaction;
    private DateTime           dateTime;

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
        time = (TextView) findViewById(R.id.time);
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
            dateTime=new DateTime(transaction.date);
        }

        updateDateTime();
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
        transaction.date = dateTime;
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
