package kalpas.expensetracker;

import kalpas.expensetracker.core.Transaction;
import kalpas.testservice.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class EditTranDetailsDialog extends DialogFragment {

    public Transaction transaction;

    private EditText   amount;
    private EditText   subject;
    private EditText   tags;
    private TextView   recipient;

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    NoticeDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_tran_details, null);
        builder.setView(view).setMessage(R.string.dialog_tran_edit_message)
                .setPositiveButton(R.string.dialog_tran_save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        transaction.amount = Double.valueOf(amount.getText().toString());
                        transaction.subject = subject.getText().toString();
                        transaction.tags = tags.getText().toString();

                        mListener.onDialogPositiveClick(EditTranDetailsDialog.this);
                    }
                }).setNegativeButton(R.string.dialog_tran_discard_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        amount = (EditText) view.findViewById(R.id.amount);
        subject = (EditText) view.findViewById(R.id.subject);
        tags = (EditText) view.findViewById(R.id.tags);
        recipient = (TextView) view.findViewById(R.id.recipient);

        recipient.setText(transaction.recipient);
        amount.setText(transaction.amount.toString());
        subject.setText(transaction.subject);
        tags.setText(transaction.subject);

        return builder.create();
    }

}
