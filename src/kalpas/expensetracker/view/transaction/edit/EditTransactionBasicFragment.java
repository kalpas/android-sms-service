package kalpas.expensetracker.view.transaction.edit;

import static kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_ADD;
import static kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_SPLIT;
import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.dateFormatMid;
import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.timeFormatMid;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.core.Transaction.TranType;
import kalpas.expensetracker.view.datetime.DatePickerFragment;
import kalpas.expensetracker.view.datetime.TimePickerFragment;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Strings;

/**
 * 
 */
public class EditTransactionBasicFragment extends Fragment implements View.OnClickListener {

    public final static String             TAG             = "kalpas.expensetracker.view.transaction.edit.EditTransactionBasicFragment";

    private static final String            ARG_TRANSACTION = "ARG_TRANSACTION";
    private static final String            ARG_ACTION      = "ARG_ACTION";

    private OnBasicEditInteractionListener hostingActivity;

    // *****************************************************

    private TextView                       dateTextView;
    private TextView                       timeTextView;

    private EditText                       amountEditText;
    private EditText                       descriptionEditText;
    private EditText                       tagsEditText;
    private TextView                       recipientTextView;

    private Transaction                    transactionModel;
    private DateTime                       tranDateModel;

    private Button                         splitButton;
    private Button                         cancelButton;
    private Button                         saveButton;

    private ImageButton                    addtagsButton;

    private ToggleButton                   tranDetailsButton;
    private ViewStub                       mTranDetails;

    private String                         action;

    private Spinner                        tranTypeSpinner;

    private ArrayAdapter<CharSequence>     tranTypeSpinnerAdapter;

    // *****************************************************

    /**
     */
    public static EditTransactionBasicFragment newInstance(Transaction trx, String action) {
        EditTransactionBasicFragment fragment = new EditTransactionBasicFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSACTION, trx);
        args.putString(ARG_ACTION, action);
        fragment.setArguments(args);
        return fragment;
    }

    public EditTransactionBasicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionModel = (Transaction) getArguments().getSerializable(ARG_TRANSACTION);
            action = getArguments().getString(ARG_ACTION);

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            hostingActivity = (OnBasicEditInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnBasicEditInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_transaction_basic, container, false);

        amountEditText = (EditText) view.findViewById(R.id.amount);
        descriptionEditText = (EditText) view.findViewById(R.id.description);
        tagsEditText = (EditText) view.findViewById(R.id.tags);
        recipientTextView = (TextView) view.findViewById(R.id.recipient);

        dateTextView = (TextView) view.findViewById(R.id.date);
        dateTextView.setOnClickListener(this);

        timeTextView = (TextView) view.findViewById(R.id.time);
        timeTextView.setOnClickListener(this);

        splitButton = (Button) view.findViewById(R.id.button_split);
        splitButton.setOnClickListener(this);

        saveButton = (Button) view.findViewById(R.id.button_save);
        saveButton.setOnClickListener(this);

        cancelButton = (Button) view.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(this);

        tranDetailsButton = (ToggleButton) view.findViewById(R.id.button_tran_details);
        tranDetailsButton.setOnClickListener(this);

        addtagsButton = (ImageButton) view.findViewById(R.id.button_add_tags);
        addtagsButton.setOnClickListener(this);

        mTranDetails = (ViewStub) view.findViewById(R.id.tran_details);
        tranTypeSpinner = (Spinner) view.findViewById(R.id.tran_type_spinner);

        tranTypeSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.tran_types,
                android.R.layout.simple_spinner_item);
        tranTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tranTypeSpinner.setAdapter(tranTypeSpinnerAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (action != null) {
            if (ACTION_ADD.equals(action) || ACTION_SPLIT.equals(action)) {
                splitButton.setVisibility(View.GONE);
                if (ACTION_ADD.equals(action)) {
                    setDateTime(new DateTime());
                }
            } else {
                setDateTime(new DateTime(transactionModel.date));
                tranTypeSpinner.setSelection(tranTypeSpinnerAdapter.getPosition(transactionModel.type.toString()));
            }
            descriptionEditText.setText(transactionModel.description);
            setRecepient(transactionModel.recipient);
            tagsEditText.setText(transactionModel.tags);
            amountEditText.setText(transactionModel.amount == null ? "" : transactionModel.amount.toString());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hostingActivity = null;
    }

    // ***************************************************

    public void startEditTags() {
        if (tagsEditText != null) {
            tagsEditText.requestFocus();
            tagsEditText.setHint(getResources().getString(R.string.new_tag_hint));
            tagsEditText.setSelection(tagsEditText.getText().length());
        }
    }

    // ***************************************************

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.button_save:
            if (!isAmountValid(amountEditText.getText().toString())) {
                Toast.makeText(getActivity(), getResources().getString(R.string.enter_amount), Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            updateWithChanges(transactionModel);
            hostingActivity.onSave(transactionModel);
            break;
        case R.id.button_split:
            if (!isAmountValid(amountEditText.getText().toString())) {
                Toast.makeText(getActivity(), getResources().getString(R.string.enter_amount), Toast.LENGTH_SHORT)
                .show();
                return;
            }
            updateWithChanges(transactionModel);
            hostingActivity.onSplit(transactionModel);
            break;
        case R.id.button_cancel:
            hostingActivity.onCancel();
            break;
        case R.id.button_add_tags:
            updateWithChanges(transactionModel);
            hostingActivity.onEditTags(transactionModel);
            break;
        case R.id.button_tran_details:
            onAdvancedTransactionDetailsClick(v);
            break;
        case R.id.date:
            showDatePickerDialog(v);
            break;
        case R.id.time:
            showTimePickerDialog(v);
            break;
        default:
            break;
        }

    }

    boolean isAmountValid(String amountString) {
        // validate input
        boolean valid = true;
        if (!Strings.isNullOrEmpty(amountString)) {
            try {
                Double value = Double.valueOf(amountString);
                if (value.equals(0.)) {
                    valid = false;
                }
            } catch (NumberFormatException e) {
                valid = false;
            }
        } else {
            valid = false;
        }
        return valid;
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

    // ***************************************************
    public void setDate(int year, int month, int day) {
        MutableDateTime newDate = tranDateModel.toMutableDateTime();
        newDate.setYear(year);
        newDate.setMonthOfYear(month + 1);
        newDate.setDayOfMonth(day);

        setDateTime(newDate.toDateTime());
    }

    public void setTime(int hourOfDay, int minute) {
        MutableDateTime newTime = tranDateModel.toMutableDateTime();
        newTime.setHourOfDay(hourOfDay);
        newTime.setMinuteOfHour(minute);

        setDateTime(newTime.toDateTime());
    }

    // ***************************************************
    private void updateWithChanges(Transaction tran) {
        tran.date = tranDateModel;
        String amountString = amountEditText.getText().toString();
        if (!Strings.isNullOrEmpty(amountString)) {
            tran.amount = Math.abs(Double.valueOf(amountString));
        } else {
            tran.amount = null;
        }
        tran.description = descriptionEditText.getText().toString();
        tran.tags = tagsEditText.getText().toString();
        tran.type = TranType.forName((String) tranTypeSpinner.getSelectedItem());
    }

    /**
     * advanced options FIXME
     */
    public void onAdvancedTransactionDetailsClick(View v) {
        ToggleButton toggle = (ToggleButton) v;
        if (toggle.isChecked()) {
            mTranDetails.setVisibility(View.VISIBLE);

        } else {
            mTranDetails.setVisibility(View.GONE);
        }
    }

    // ***************************************************

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

    // ***************************************************

    /**
     */
    public interface OnBasicEditInteractionListener {
        public void onCancel();

        public void onSave(Transaction transaction);

        public void onSplit(Transaction transaction);

        public void onEditTags(Transaction transaction);
    }

}
