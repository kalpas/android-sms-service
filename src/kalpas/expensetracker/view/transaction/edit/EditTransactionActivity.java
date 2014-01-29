package kalpas.expensetracker.view.transaction.edit;

import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.dateFormatMid;
import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.timeFormatMid;
import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.core.Transaction.TranType;
import kalpas.expensetracker.view.datetime.DatePickerFragment;
import kalpas.expensetracker.view.datetime.TimePickerFragment;
import kalpas.expensetracker.view.suggestions.SuggestionsFragment;
import kalpas.expensetracker.view.transaction.edit.tags.TagSelectionFragment;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Strings;

public class EditTransactionActivity extends Activity implements TimePickerFragment.TimeSetListener,
        DatePickerFragment.DateSetListener, TagSelectionFragment.OnTagsSelectedListener {

    private static final int           REQUEST_CODE_SPLIT = 1;
    public static final String         ACTION_EDIT        = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_EDIT";
    public static final String         ACTION_SPLIT       = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_SPLIT";
    public static final String         ACTION_ADD         = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_ADD";

    private TextView                   dateTextView;
    private TextView                   timeTextView;

    private EditText                   amountEditText;
    private EditText                   descriptionEditText;
    private EditText                   tagsEditText;
    private TextView                   recipientTextView;

    private Transaction                transactionModel;
    private DateTime                   tranDateModel;

    private Button                     splitButton;

    private FrameLayout                advancedTranDetailsView;

    private String                     currentAction;

    private Spinner                    tranTypeSpinner;

    private ArrayAdapter<CharSequence> tranTypeSpinnerAdapter;

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

        amountEditText = (EditText) findViewById(R.id.amount);
        descriptionEditText = (EditText) findViewById(R.id.description);
        tagsEditText = (EditText) findViewById(R.id.tags);
        recipientTextView = (TextView) findViewById(R.id.recipient);
        dateTextView = (TextView) findViewById(R.id.date);
        timeTextView = (TextView) findViewById(R.id.time);

        splitButton = (Button) findViewById(R.id.button_split);

        advancedTranDetailsView = (FrameLayout) findViewById(R.id.tran_details);
        tranTypeSpinner = (Spinner) findViewById(R.id.tran_type_spinner);

        tranTypeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.tran_types,
                android.R.layout.simple_spinner_item);
        tranTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tranTypeSpinner.setAdapter(tranTypeSpinnerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        currentAction = intent == null ? null : intent.getAction();
        if (currentAction != null) {
            if (ACTION_ADD.equals(currentAction)) {
                splitButton.setVisibility(View.GONE);

                setDateTime(new DateTime());
                setRecepient(null);
            } else {
                transactionModel = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);
                setDateTime(new DateTime(transactionModel.date));
                setRecepient(transactionModel.recipient);
                tranTypeSpinner.setSelection(tranTypeSpinnerAdapter.getPosition(transactionModel.type.toString()));

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

    private void setDateTime(DateTime newDateTime) {
        tranDateModel = newDateTime;
        dateTextView.setText(dateFormatMid.print(tranDateModel));
        timeTextView.setText(timeFormatMid.print(tranDateModel));
    }

    private void setRecepient(String recipient) {
        if (Strings.isNullOrEmpty(recipient)) {
            recipientTextView.setVisibility(View.GONE);
        } else {
            recipientTextView.setVisibility(View.VISIBLE);
            recipientTextView.setText(recipient);
        }
    }

    private void updateWithChanges(Transaction trx) {
        trx.date = tranDateModel;
        trx.amount = Math.abs(Double.valueOf(amountEditText.getText().toString()));
        trx.description = descriptionEditText.getText().toString();
        trx.tags = tagsEditText.getText().toString();
        trx.type = TranType.forName((String) tranTypeSpinner.getSelectedItem());
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
        // validate input
        try {
            Double.valueOf(amountEditText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, getResources().getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }

        if (ACTION_EDIT.equals(currentAction)) {
            updateWithChanges(transactionModel);
            sendUpdate(transactionModel);

        } else {
            Transaction trx;
            trx = new Transaction(tranDateModel);

            trx.amount = Math.abs(Double.valueOf(amountEditText.getText().toString()));
            if (ACTION_SPLIT.equals(currentAction) && Math.abs(trx.amount) >= Math.abs(transactionModel.amount)) {
                Toast.makeText(this, getResources().getString(R.string.split_amount_warning), Toast.LENGTH_SHORT)
                        .show();
                return;// skip update
            }

            trx.type = TranType.forName((String) tranTypeSpinner.getSelectedItem());

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
        }

        this.finish();
        return;
    }

    /**
     * add tagsEditText onClick
     */
    public void onAddTagsClick(View v) {
        TagSelectionFragment.newInstance(transactionModel).show(getFragmentManager(), TagSelectionFragment.TAG);
    }

    /**
     * advanced options
     */
    public void onAdvancedTransactionDetailsClick(View v) {
        ToggleButton toggle = (ToggleButton) v;
        if (toggle.isChecked()) {
            advancedTranDetailsView.setVisibility(View.VISIBLE);
            SuggestionsFragment fragment = SuggestionsFragment.newInstance(transactionModel);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.tran_details, fragment, SuggestionsFragment.TAG);
            transaction.commit();

        } else {
            advancedTranDetailsView.setVisibility(View.GONE);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.remove(getFragmentManager().findFragmentByTag(SuggestionsFragment.TAG));
            transaction.commit();
        }
    }

    @Override
    public void onTagsSelected(String tags) {
        if (!Strings.isNullOrEmpty(tags)) {
            String oldValue = tagsEditText.getText().toString();
            if (!oldValue.isEmpty() && !oldValue.trim().endsWith(",")) {
                oldValue += ", ";
            }
            tagsEditText.setText(oldValue + tags + ",");
        }

        tagsEditText.requestFocus();
        tagsEditText.setHint(getResources().getString(R.string.new_tag_hint));
        tagsEditText.setSelection(tagsEditText.getText().length());
    }
}
