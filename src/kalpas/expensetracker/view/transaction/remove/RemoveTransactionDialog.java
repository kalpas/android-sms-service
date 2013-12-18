package kalpas.expensetracker.view.transaction.remove;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class RemoveTransactionDialog extends DialogFragment {

    public interface RemoveTransactionListener {
        public void onDialogPositiveClick(RemoveTransactionDialog dialog);
    }
    
    public Transaction transaction;
    
    private RemoveTransactionListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (RemoveTransactionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.remove_transaction_message)
                .setPositiveButton(R.string.remove_transaction, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(RemoveTransactionDialog.this);

                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

}
