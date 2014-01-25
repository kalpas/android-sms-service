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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Joiner;

//TODO extract tag selection component
public class EditTransactionActivity extends Activity implements TimePickerFragment.TimeSetListener,
        DatePickerFragment.DateSetListener {

    private static final int   REQUEST_CODE_SPLIT = 1;
    public static final String ACTION_EDIT        = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_EDIT";
    public static final String ACTION_SPLIT       = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_SPLIT";
    public static final String ACTION_ADD         = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_ADD";

    private TextView           dateTextView;
    private TextView           timeTextView;

    private EditText           amountEditText;
    private EditText           descriptionEditText;
    private EditText           tagsEditText;
    private TextView           recipientTextView;

    private Transaction        transactionModel;
    private DateTime           tranDateModel;

    private ScrollView         tagsScrollView;
    private LinearLayout       tagsContainerLayout;

    private ImageButton        addTagsButton;
    private ImageButton        acceptTagsButton;

    private ToggleButton       signToggle;

    private Button             splitButton;

    private LinearLayout       advancedTranDetailsView;
    private Spinner            tranTypeSpinner;

    private List<String>       toggledTags        = new ArrayList<String>();

    private String             currentAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_transaction);

        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onDestroy() {
        setIntent(null);
        transactionModel = null;
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

        tagsScrollView = (ScrollView) findViewById(R.id.tag_list_parent);
        tagsContainerLayout = (LinearLayout) findViewById(R.id.tag_list);

        amountEditText = (EditText) findViewById(R.id.amount);
        descriptionEditText = (EditText) findViewById(R.id.description);
        tagsEditText = (EditText) findViewById(R.id.tags);
        recipientTextView = (TextView) findViewById(R.id.recipient);
        dateTextView = (TextView) findViewById(R.id.date);
        timeTextView = (TextView) findViewById(R.id.time);

        addTagsButton = (ImageButton) findViewById(R.id.button_add_tags);
        acceptTagsButton = (ImageButton) findViewById(R.id.button_accept_tags);

        signToggle = (ToggleButton) findViewById(R.id.sign);

        splitButton = (Button) findViewById(R.id.button_split);

        advancedTranDetailsView = (LinearLayout) findViewById(R.id.tran_details);
        tranTypeSpinner = (Spinner) findViewById(R.id.spinner_tran_type);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        currentAction = intent == null ? null : intent.getAction();
        if (currentAction != null) {
            if (ACTION_ADD.equals(currentAction)) {
                splitButton.setVisibility(View.GONE);
                signToggle.setVisibility(View.VISIBLE);

                setDateTime(new DateTime());
                setRecepirnt(null);
            } else {
                transactionModel = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);
                setDateTime(new DateTime(transactionModel.date));
                setRecepirnt(transactionModel.recipient);
                if (ACTION_EDIT.equals(currentAction)) {
                    descriptionEditText.setText(transactionModel.description);
                    tagsEditText.setText(transactionModel.tags);

                    amountEditText.setText(transactionModel.amount.toString());
                } else if (ACTION_SPLIT.equals(currentAction)) {
                    amountEditText.setText(transactionModel.amount.toString());
                    splitButton.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_CODE_SPLIT:
            if (resultCode == Activity.RESULT_OK) {
                setIntent(data);
            }
            break;
        default:
            break;
        }
    }

    private void sendSplit(Transaction trxToSplit) {
        Intent splitIntent = new Intent(this, EditTransactionActivity.class);
        splitIntent.setAction(EditTransactionActivity.ACTION_SPLIT);
        splitIntent.putExtra(BackgroundService.EXTRA_TRANSACTION, trxToSplit);
        startActivityForResult(splitIntent, REQUEST_CODE_SPLIT);
    }

    private void sendUpdate(Transaction trx) {
        Intent update = new Intent(this, BackgroundService.class);
        update.setAction(BackgroundService.ACTION_UPDATE);
        update.putExtra(BackgroundService.EXTRA_TRANSACTION, trx);
        startService(update);
    }

    private void showTagSelection() {
        Tags tagProvider = Tags.getTagsProvider();
        List<String> tagsList = tagProvider.getTags(this);

        addTagsButton.setVisibility(View.GONE);
        acceptTagsButton.setVisibility(View.VISIBLE);

        tagsEditText.setVisibility(View.GONE);

        tagsScrollView.setVisibility(View.VISIBLE);

        tagsContainerLayout.addView(createNewTagButton());
        for (String tag : tagsList) {
            // tagsContainerLayout.addView(createDivider());
            tagsContainerLayout.addView(createTagButton(tag));
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
        tagsScrollView.setVisibility(View.GONE);
        tagsContainerLayout.removeAllViews();

        addTagsButton.setVisibility(View.VISIBLE);
        acceptTagsButton.setVisibility(View.GONE);

        tagsEditText.setVisibility(View.VISIBLE);
    }

    private void setDateTime(DateTime newDateTime) {
        tranDateModel = newDateTime;
        dateTextView.setText(dateFormatMid.print(tranDateModel));
        timeTextView.setText(timeFormatMid.print(tranDateModel));
    }

    private void setRecepirnt(String recipient) {
        if (StringUtils.isEmpty(recipient)) {
            recipientTextView.setVisibility(View.GONE);
        } else {
            recipientTextView.setVisibility(View.VISIBLE);
            recipientTextView.setText(recipient);
        }
    }

    private void updateWithChanges(Transaction trx) {
        trx.date = tranDateModel;
        trx.amount = Double.valueOf(amountEditText.getText().toString());
        trx.description = descriptionEditText.getText().toString();
        trx.tags = tagsEditText.getText().toString();
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
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        return layoutParams;
    }

    // ******************** Misc. handlers ***********************************

    /**
     * DatePickerFragment.DateSetListener
     */
    @Override
    public void onDateSet(int year, int month, int day) {
        MutableDateTime newDate = tranDateModel.toMutableDateTime();
        newDate.setYear(year);
        newDate.setMonthOfYear(month + 1);
        newDate.setDayOfMonth(day);

        setDateTime(newDate.toDateTime());
    }

    /**
     * TimePickerFragment.TimeSetListener
     */
    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        MutableDateTime newTime = tranDateModel.toMutableDateTime();
        newTime.setHourOfDay(hourOfDay);
        newTime.setMinuteOfHour(minute);

        setDateTime(newTime.toDateTime());
    }

    /**
     * Date onClick()
     * 
     * @param v
     *            View
     */
    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.date = tranDateModel;
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
        newFragment.time = tranDateModel;
        newFragment.show(getFragmentManager(), "timePicker");
    }

    /**
     * Button discard onClick()
     * 
     * @param view
     */
    public void onCancel(View view) {
        setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

    /**
     * Button split onClick()
     * 
     * @param view
     */
    public void onSplit(View view) {
        updateWithChanges(transactionModel);
        sendSplit(transactionModel);
    }

    /**
     * Button update onClick()
     * 
     * @param view
     */
    public void onUpdate(View view) {

        if (ACTION_EDIT.equals(currentAction)) {
            updateWithChanges(transactionModel);
            sendUpdate(transactionModel);
            this.finish();
            return;
        }

        Transaction trx;
        trx = new Transaction(tranDateModel);

        try {
            trx.amount = Double.valueOf(amountEditText.getText().toString());
            if (ACTION_SPLIT.equals(currentAction) && Math.abs(trx.amount) >= Math.abs(transactionModel.amount)) {
                Toast.makeText(this, getResources().getString(R.string.split_amount_warning), Toast.LENGTH_SHORT)
                        .show();
                return;
            } else if (View.GONE != signToggle.getVisibility() && !signToggle.isChecked() && trx.amount > 0) {
                trx.amount = -trx.amount;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getResources().getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }

        trx.description = descriptionEditText.getText().toString();
        trx.tags = tagsEditText.getText().toString();

        Intent addTrxIntent = new Intent(this, BackgroundService.class);
        addTrxIntent.setAction(BackgroundService.ACTION_ADD);
        addTrxIntent.putExtra(BackgroundService.EXTRA_TRANSACTION, trx);
        startService(addTrxIntent);

        if (currentAction != null && ACTION_SPLIT.equals(currentAction)) {
            transactionModel.amount -= trx.amount;
            sendUpdate(transactionModel);

            Intent result = new Intent(this, EditTransactionActivity.class);
            result.setAction(EditTransactionActivity.ACTION_EDIT);
            result.putExtra(BackgroundService.EXTRA_TRANSACTION, transactionModel);
            setResult(Activity.RESULT_OK, result);
        }

        this.finish();
    }

    /**
     * add tagsEditText onClick
     */
    public void onAcceptTagsClick(View v) {
        if (!toggledTags.isEmpty()) {
            String oldValue = tagsEditText.getText().toString();
            if (!oldValue.isEmpty() && !oldValue.trim().endsWith(",")) {
                oldValue += ", ";
            }
            tagsEditText.setText(oldValue + Joiner.on(", ").skipNulls().join(toggledTags) + ",");
            toggledTags.clear();
        }

        hideTagSelection();

        tagsEditText.requestFocus();
        tagsEditText.setHint(getResources().getString(R.string.new_tag_hint));
        tagsEditText.setSelection(tagsEditText.getText().length());
    }

    /**
     * add tagsEditText onClick
     */
    public void onAddTagsClick(View v) {
        showTagSelection();
    }

    /**
     * advanced options
     */
    public void onAdvancedTransactionDetailsClick(View v) {
        ToggleButton toggle = (ToggleButton) v;
        if (toggle.isChecked()) {
            advancedTranDetailsView.setVisibility(View.VISIBLE);
            ArrayAdapter<CharSequence> tranTypeAdapter = ArrayAdapter.createFromResource(this, R.array.tran_types,
                    android.R.layout.simple_spinner_item);
            tranTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            tranTypeSpinner.setAdapter(tranTypeAdapter);

        } else {
            advancedTranDetailsView.setVisibility(View.GONE);
        }
    }

    // FIXME
    // /**
    // * mark cash onClick()
    // */
    // public void onMarkAsCashClick(View v) {
    // transactionModel.tranType = TranType.WITHDRAWAL;
    // tagsEditText.setText("cash");
    // hideTagSelectionControls();
    // showBasicControls();
    // }
}
