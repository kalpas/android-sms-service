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
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
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

    private ScrollView         scrollTagsView;
    private LinearLayout       tagsContainer;

    private Button             splitButton;
    private Button             updateButton;
    @SuppressWarnings("unused")
    private Button             discardButton;

    private List<String>       toggledTags       = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
    protected void onStart() {
        super.onStart();

        parent = (LinearLayout) findViewById(R.id.parent);
        scrollTagsView = (ScrollView) findViewById(R.id.tag_list_parent);
        tagsContainer = (LinearLayout) findViewById(R.id.tag_list);

        amount = (EditText) findViewById(R.id.amount);
        description = (EditText) findViewById(R.id.description);
        tags = (EditText) findViewById(R.id.tags);
        recipient = (TextView) findViewById(R.id.recipient);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);

        splitButton = (Button) findViewById(R.id.button_split);
        updateButton = (Button) findViewById(R.id.button_update);
        discardButton = (Button) findViewById(R.id.button_discard);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent == null ? null : intent.getAction();

        if (ACTION_EDIT.equals(action) || ACTION_SAVE_SPLIT.equals(action)) {
            transaction = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);

            if (StringUtils.isEmpty(transaction.tags)) {
                showInteractiveTagSelection();// show tag list
            } else {
                parent.removeView(scrollTagsView);
                showBasicControls();
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
            updateDateTime();
        }

    }

    private ToggleButton createTagButton(String tag) {
        ToggleButton itemView;
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources()
                .getDisplayMetrics());
        int margin4dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        int margin16dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources()
                .getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin16dp, 0, margin4dp, 0);

        itemView = new ToggleButton(this, null, android.R.attr.buttonBarButtonStyle);
        itemView.setLayoutParams(layoutParams);
        itemView.setMinimumHeight(height);
        itemView.setMinHeight(height);
        itemView.setText(tag);
        itemView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton button = (ToggleButton) v;
                if (button.isChecked()) {
                    toggledTags.add(button.getText().toString());
                    button.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
                } else {
                    toggledTags.remove(button.getText().toString());
                    button.setTextColor(getResources().getColor(android.R.color.white));
                }
            }
        });

        return itemView;
    }

    private ImageView createDivider() {
        int margin4dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        int margin16dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources()
                .getDisplayMetrics());

        ImageView divider = new ImageView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(margin16dp, 0, margin4dp, 0);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(Color.GRAY);
        return divider;
    }

    private void hideBasicControls() {
        splitButton.setVisibility(View.GONE);
        updateButton.setVisibility(View.GONE);

        description.setVisibility(View.GONE);
        tags.setVisibility(View.GONE);
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
        hideBasicControls();

        Tags tagProvider = Tags.getTagsProvider();
        List<String> tagsList = tagProvider.getTags(this);

        for (String tag : tagsList) {
            tagsContainer.addView(createTagButton(tag));
            tagsContainer.addView(createDivider());
        }

    }

    private void showBasicControls() {
        splitButton.setVisibility(View.VISIBLE);
        updateButton.setVisibility(View.VISIBLE);

        tags.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
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

    // ******************** Misc. handlers ***********************************

    /**
     * DatePickerFragment.DateSetListener
     */
    @Override
    public void onDateSet(int year, int month, int day) {
        MutableDateTime newDate = dateTime.toMutableDateTime();
        newDate.setYear(year);
        newDate.setMonthOfYear(month + 1);
        newDate.setDayOfMonth(day);
        dateTime = newDate.toDateTime();
        updateDateTime();
    }

    /**
     * TimePickerFragment.TimeSetListener
     */
    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        MutableDateTime newTime = dateTime.toMutableDateTime();
        newTime.setHourOfDay(hourOfDay);
        newTime.setMinuteOfHour(minute);
        dateTime = newTime.toDateTime();
        updateDateTime();
    }

    /**
     * Date onClick()
     * 
     * @param v
     *            View
     */
    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.date = dateTime;
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Time onClick()
     * 
     * @param v
     *            View
     */
    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.time = dateTime;
        newFragment.show(getFragmentManager(), "timePicker");
    }

    /**
     * Button discard onClick()
     * 
     * @param view
     */
    public void discard(View view) {
        this.finish();
    }

    /**
     * Button split onClick()
     * 
     * @param view
     */
    public void split(View view) {
        updateWithChanges();
        sendSplit();
    }

    /**
     * Button update onClick()
     * 
     * @param view
     */
    public void update(View view) {
        update();
        this.finish();
    }

    /**
     * add tags onClick
     */
    public void onAddTagsClick(View v) {
        tags.setText(Joiner.on(", ").skipNulls().join(toggledTags));
        parent.removeView(scrollTagsView);
        showBasicControls();
    }

    /**
     * mark cash onClick()
     */
    public void onMarkAsCashClick(View v) {
        transaction.tranType = TranType.WITHDRAWAL;
        tags.setText("cash");
        parent.removeView(scrollTagsView);
        showBasicControls();
    }

}
