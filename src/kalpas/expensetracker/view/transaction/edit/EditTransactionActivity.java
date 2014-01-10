package kalpas.expensetracker.view.transaction.edit;

import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.dateFormatMid;
import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.timeFormatMid;

import java.util.ArrayList;
import java.util.List;

import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Tags;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.core.Transaction.TranType;
import kalpas.expensetracker.view.datetime.DatePickerFragment;
import kalpas.expensetracker.view.datetime.TimePickerFragment;
import kalpas.expensetracker.view.transaction.add.AddTransactionActivity;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.base.Joiner;

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

    private ScrollView         tagGridParent;
    private GridLayout         tagGrid;

    private List<String>       toggledTags       = new ArrayList<String>();

    public void discard(View view) {
        this.finish();
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

    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        MutableDateTime newTime = dateTime.toMutableDateTime();
        newTime.setHourOfDay(hourOfDay);
        newTime.setMinuteOfHour(minute);
        dateTime = newTime.toDateTime();
        updateDateTime();
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.date = dateTime;
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.time = dateTime;
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void split(View view) {
        updateWithChanges();
        sendSplit();
    }

    public void update(View view) {
        update();
        this.finish();
    }

    private Button createCashButton() {
        Button cashButton = new Button(this, null, android.R.attr.buttonBarButtonStyle);
        cashButton.setText("Cash");
        cashButton.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        cashButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                transaction.tranType = TranType.WITHDRAWAL;
                showTextEditControls();
            }
        });
        return cashButton;
    }

    private Button createOkButton() {
        Button okButton = new Button(this, null, android.R.attr.buttonBarButtonStyle);
        okButton.setText(getResources().getString(R.string.done));
        okButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tags.setText(Joiner.on(", ").skipNulls().join(toggledTags));
                showTextEditControls();
            }
        });
        return okButton;
    }

    private ToggleButton createTagButton(String tag) {
        ToggleButton view;
        view = new ToggleButton(this, null, android.R.attr.buttonBarButtonStyle);
        view.setText(tag);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setGravity(Gravity.CENTER_HORIZONTAL);
        view.setLayoutParams(params);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton button = (ToggleButton) v;
                if (button.isChecked()) {
                    toggledTags.add(button.getText().toString());
                    button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                } else {
                    toggledTags.remove(button.getText().toString());
                    button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            }
        });
        return view;
    }

    private void hideTextEditControls() {
        description.setVisibility(View.GONE);
        tags.setVisibility(View.GONE);
        amount.setEnabled(false);
    }

    private void sendSplit() {
        Intent splitIntent = new Intent(this, AddTransactionActivity.class);
        splitIntent.setAction(AddTransactionActivity.ACTION_SPLIT);
        splitIntent.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        startActivity(splitIntent);
    }

    private void sendUpdate() {
        Intent update = new Intent(this, BackgroundService.class);
        update.setAction(BackgroundService.ACTION_UPDATE);
        update.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        startService(update);
    }

    private void showInteractiveTagSelection() {
        hideTextEditControls();

        Tags tagProvider = new Tags();
        List<String> tagList = tagProvider.getTags(this);
        int rows = Math.round((tagList.size() + 1) / 3);
        tagGrid.setRowCount(rows);

        Button cashButton = createCashButton();
        tagGrid.addView(cashButton);

        ToggleButton view;
        for (String tag : tagList) {
            view = createTagButton(tag);
            tagGrid.addView(view);
        }

        Button okButton = createOkButton();
        tagGrid.addView(okButton);
    }

    private void showTextEditControls() {
        parent.removeView(tagGridParent);
        tags.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        amount.setEnabled(true);
    }

    private void update() {
        updateWithChanges();
        sendUpdate();
    }

    private void updateDateTime() {
        date.setText(dateFormatMid.print(dateTime));
        time.setText(timeFormatMid.print(dateTime));
    }

    private void updateWithChanges() {
        transaction.date = dateTime;
        transaction.amount = Double.valueOf(amount.getText().toString());
        transaction.description = description.getText().toString();
        transaction.tags = tags.getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);
    }

    @Override
    protected void onDestroy() {
        setIntent(null);
        transaction = null;
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent == null ? null : intent.getAction();

        if (ACTION_EDIT.equals(action) || ACTION_SAVE_SPLIT.equals(action)) {
            transaction = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);

            if (StringUtils.isEmpty(transaction.tags)) {
                showInteractiveTagSelection();
            } else {
                showTextEditControls();
            }

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
            dateTime = new DateTime(transaction.date);
        }

        updateDateTime();
    }

    @Override
    protected void onStart() {
        super.onStart();

        parent = (LinearLayout) findViewById(R.id.parent);
        tagGrid = (GridLayout) findViewById(R.id.tagGrid);
        tagGridParent = (ScrollView) findViewById(R.id.tagGridParent);

        amount = (EditText) findViewById(R.id.amount);
        description = (EditText) findViewById(R.id.description);
        tags = (EditText) findViewById(R.id.tags);
        recipient = (TextView) findViewById(R.id.recipient);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
    }
}
