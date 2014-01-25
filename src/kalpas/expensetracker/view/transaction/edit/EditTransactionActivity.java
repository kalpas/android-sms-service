package kalpas.expensetracker.view.transaction.edit;

import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.dateFormatMid;
import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.timeFormatMid;

import java.util.ArrayList;
import java.util.List;

import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Tags;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.datetime.DatePickerFragment;
import kalpas.expensetracker.view.datetime.TimePickerFragment;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Joiner;

//TODO extract tag selection component
public class EditTransactionActivity extends Activity implements TimePickerFragment.TimeSetListener,
        DatePickerFragment.DateSetListener {

    public static final String ACTION_EDIT       = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_EDIT";
    public static final String ACTION_SAVE_SPLIT = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_SAVE_SPLIT";
    public static final String ACTION_SPLIT      = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_SPLIT";

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

    private ImageButton        btAddTags;
    private ImageButton        btAcceptTags;

    private ToggleButton       sign;

    private List<String>       toggledTags       = new ArrayList<String>();

    private String             action;

    private int                number;

    private static class Counter {
        private static int count = 0;

        public static void increment() {
            count++;
        }

        public static int getCount() {
            return count;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_transaction);

        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        Counter.increment();
        number = Counter.getCount();
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

        btAddTags = (ImageButton) findViewById(R.id.button_add_tags);
        btAcceptTags = (ImageButton) findViewById(R.id.button_accept_tags);

        sign = (ToggleButton) findViewById(R.id.sign);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent(); 
        action = intent == null ? null : intent.getAction();
        if (action != null) {

            transaction = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);
            if (ACTION_EDIT.equals(action) || ACTION_SAVE_SPLIT.equals(action)) {

                if (ACTION_SAVE_SPLIT.equals(action)) {
                    sendUpdate();// returning from split screen, need to save
                                 // changes to the original transaction
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
            } else if (ACTION_SPLIT.equals(action)) {
                amount.setText(transaction.amount.toString());
                dateTime = transaction.date;
            }
            updateDateTime();

            description.setText(Integer.toString(number));
        }

    }

    private void sendSplit() {
        Intent splitIntent = new Intent(this, EditTransactionActivity.class);
        splitIntent.setAction(EditTransactionActivity.ACTION_SPLIT);
        splitIntent.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        startActivity(splitIntent);
    }

    private void sendUpdate() {
        Intent update = new Intent(this, BackgroundService.class);
        update.setAction(BackgroundService.ACTION_UPDATE);
        update.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        startService(update);
    }

    private void showTagSelection() {
        Tags tagProvider = Tags.getTagsProvider();
        List<String> tagsList = tagProvider.getTags(this);

        btAddTags.setVisibility(View.GONE);
        btAcceptTags.setVisibility(View.VISIBLE);

        tags.setVisibility(View.GONE);

        scrollTagsView.setVisibility(View.VISIBLE);

        tagsContainer.addView(createNewTagButton());
        for (String tag : tagsList) {
            // tagsContainer.addView(createDivider());
            tagsContainer.addView(createTagButton(tag));
        }

    }

    private TextView createNewTagButton() {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources()
                .getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = getTagListLayoutParams();

        Button newTag = new Button(this, null, android.R.attr.buttonBarButtonStyle);
        newTag.setLayoutParams(layoutParams);
        newTag.setTypeface(null, Typeface.ITALIC);
        newTag.setText(getResources().getString(R.string.new_tag));
        newTag.setClickable(true);
        newTag.setMinimumHeight(height);
        newTag.setMinHeight(height);
        newTag.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        newTag.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onAcceptTagsClick(v);
            }
        });
        return newTag;
    }

    private void hideTagSelection() {
        scrollTagsView.setVisibility(View.GONE);
        tagsContainer.removeAllViews();

        btAddTags.setVisibility(View.VISIBLE);
        btAcceptTags.setVisibility(View.GONE);

        tags.setVisibility(View.VISIBLE);
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

    private ToggleButton createTagButton(String tag) {
        ToggleButton itemView;
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources()
                .getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = getTagListLayoutParams();

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
                    button.setBackgroundColor(getResources().getColor(R.color.highlight));
                } else {
                    toggledTags.remove(button.getText().toString());
                    button.setTextColor(getResources().getColor(android.R.color.white));
                    button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            }
        });

        return itemView;
    }

    private LinearLayout.LayoutParams getTagListLayoutParams() {
        int margin4dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin4dp, 0, margin4dp, 0);
        return layoutParams;
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
    public void onSplit(View view) {
        updateWithChanges();
        sendSplit();
    }

    /**
     * Button update onClick()
     * 
     * @param view
     */
    public void update(View view) {

        if (ACTION_SPLIT.equals(action)) {
            //
            Transaction trx;

            trx = new Transaction(dateTime);

            try {
                trx.amount = Double.valueOf(amount.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(this, getResources().getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!sign.isChecked() && trx.amount > 0) {
                trx.amount = -trx.amount;
            }
            trx.description = description.getText().toString();
            trx.tags = tags.getText().toString();

            Intent update = new Intent(this, BackgroundService.class);
            update.setAction(BackgroundService.ACTION_ADD);
            update.putExtra(BackgroundService.EXTRA_TRANSACTION, trx);
            startService(update);

            if (action != null && ACTION_SPLIT.equals(action)) {
                transaction.amount -= trx.amount;

                Intent intent = new Intent(this, EditTransactionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setAction(EditTransactionActivity.ACTION_SAVE_SPLIT);
                intent.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
                startActivity(intent);
            }

        } else {
            updateWithChanges();
            sendUpdate();
        }

        this.finish();
    }

    /**
     * add tags onClick
     */
    public void onAcceptTagsClick(View v) {
        if (!toggledTags.isEmpty()) {
            String oldValue = tags.getText().toString();
            if (!oldValue.isEmpty() && !oldValue.trim().endsWith(",")) {
                oldValue += ", ";
            }
            tags.setText(oldValue + Joiner.on(", ").skipNulls().join(toggledTags) + ",");
            toggledTags.clear();
        }

        hideTagSelection();

        tags.requestFocus();
        tags.setHint(getResources().getString(R.string.new_tag_hint));
        tags.setSelection(tags.getText().length());
    }

    /**
     * add tags onClick
     */
    public void onAddTagsClick(View v) {
        showTagSelection();
    }

    // /**
    // * mark cash onClick()
    // */
    // public void onMarkAsCashClick(View v) {
    // transaction.tranType = TranType.WITHDRAWAL;
    // tags.setText("cash");
    // hideTagSelectionControls();
    // showBasicControls();
    // }
}
