package kalpas.expensetracker.view.transaction.edit;

import kalpas.expensetracker.BackgroundService;
import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.datetime.DatePickerFragment;
import kalpas.expensetracker.view.datetime.TimePickerFragment;
import kalpas.expensetracker.view.transaction.edit.tags.TagSelectionFragment;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class EditTransactionActivity extends Activity implements TagSelectionFragment.OnTagsSelectedListener,
        EditTransactionBasicFragment.OnBasicEditInteractionListener, TimePickerFragment.TimeSetListener,
        DatePickerFragment.DateSetListener {
    private static final int   REQUEST_CODE_SPLIT = 1;

    public static final String ACTION_EDIT        = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_EDIT";
    public static final String ACTION_SPLIT       = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_SPLIT";
    public static final String ACTION_ADD         = "kalpas.expensetracker.view.transaction.edit.EditTransactionActivity.ACTION_ADD";

    private String             action;

    private Transaction        mTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_transaction);

        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        action = intent == null ? null : intent.getAction();
        Transaction extra = (Transaction) intent.getSerializableExtra(BackgroundService.EXTRA_TRANSACTION);

        Transaction transactionToEdit = null;
        if (action != null) {
            if (ACTION_ADD.equals(action)) {
                transactionToEdit = new Transaction(new DateTime());
            } else if (ACTION_EDIT.equals(action)) {
                transactionToEdit = extra;
            } else if (ACTION_SPLIT.equals(action)) {
                mTransaction = extra;

                transactionToEdit = new Transaction(new DateTime(extra.date));
                transactionToEdit.amount = extra.amount;
                transactionToEdit.type = extra.type;
            }
        }

        EditTransactionBasicFragment fragment = EditTransactionBasicFragment.newInstance(transactionToEdit, action);
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment, EditTransactionBasicFragment.TAG).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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

    // ******************** Misc. handlers ***********************************

    private void startSplit(Transaction trxToSplit) {
        Intent splitIntent = new Intent(this, EditTransactionActivity.class);
        splitIntent.setAction(EditTransactionActivity.ACTION_SPLIT);
        splitIntent.putExtra(BackgroundService.EXTRA_TRANSACTION, trxToSplit);
        startActivityForResult(splitIntent, REQUEST_CODE_SPLIT);
    }

    private void startUpdate(Transaction trx) {
        Intent update = new Intent(this, BackgroundService.class);
        update.setAction(BackgroundService.ACTION_UPDATE);
        update.putExtra(BackgroundService.EXTRA_TRANSACTION, trx);
        this.startService(update);
    }

    private void startAdd(Transaction transaction) {
        Intent addTrxIntent = new Intent(this, BackgroundService.class);
        addTrxIntent.setAction(BackgroundService.ACTION_ADD);
        addTrxIntent.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        startService(addTrxIntent);
    }

    // ***************************************************

    /**
     * DatePickerFragment.DateSetListener
     */
    @Override
    public void onDateSet(int year, int month, int day) {
        EditTransactionBasicFragment fragment = (EditTransactionBasicFragment) getFragmentManager().findFragmentByTag(
                EditTransactionBasicFragment.TAG);

        fragment.setDate(year, month, day);
    }

    /**
     * TimePickerFragment.TimeSetListener
     */
    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        EditTransactionBasicFragment fragment = (EditTransactionBasicFragment) getFragmentManager().findFragmentByTag(
                EditTransactionBasicFragment.TAG);

        fragment.setTime(hourOfDay, minute);
    }

    // ************TagSelectionFragment***********************

    /**
     * {@link TagSelectionFragment#acceptTags}
     */
    @Override
    public void onTagsSelected(Transaction transaction) {
        EditTransactionBasicFragment fragment = EditTransactionBasicFragment.newInstance(transaction, ACTION_EDIT);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        fragment.startEditTags();
    }

    /**
     * {@link TagSelectionFragment#dismiss)
     */
    @Override
    public void onDismiss() {
        getFragmentManager().popBackStack();
    }

    // ************EditTransactionBasicFragment.OnBasicEditInteractionListener***********************
    /**
     * {@link EditTransactionBasicFragment.OnBasicEditInteractionListener#onCancel()}
     */
    @Override
    public void onCancel() {
        setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

    @Override
    public void onSave(Transaction transaction) {

        if (ACTION_EDIT.equals(action)) {
            startUpdate(transaction);
        } else {

            if (ACTION_SPLIT.equals(action) && Math.abs(transaction.amount) >= Math.abs(mTransaction.amount)) {
                Toast.makeText(this, getResources().getString(R.string.split_amount_warning), Toast.LENGTH_SHORT)
                        .show();
                return;// skip update
            }

            startAdd(transaction);

            if (action != null && ACTION_SPLIT.equals(action)) {
                mTransaction.amount -= transaction.amount;
                startUpdate(mTransaction);
                returnResult(mTransaction);
            }
        }

        this.finish();
    }

    private void returnResult(Transaction transaction) {
        Intent result = new Intent(this, EditTransactionActivity.class);
        result.setAction(EditTransactionActivity.ACTION_EDIT);
        result.putExtra(BackgroundService.EXTRA_TRANSACTION, transaction);
        setResult(Activity.RESULT_OK, result);
    }

    @Override
    public void onSplit(Transaction transaction) {
        startSplit(transaction);
    }

    @Override
    public void onEditTags(Transaction transaction) {
        mTransaction = transaction;
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, TagSelectionFragment.newInstance(mTransaction));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
