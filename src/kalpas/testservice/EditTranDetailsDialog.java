package kalpas.testservice;

import kalpas.testservice.core.Transaction;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EditTranDetailsDialog extends DialogFragment {
    
    public Transaction transaction;
    
    private EditText amount;
    private EditText subject;
    private EditText tags;
    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_tran_details, null);
        builder.setView(view)
                .setMessage(R.string.dialog_tran_edit_message)
                .setPositiveButton(R.string.dialog_tran_edit_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
        
        amount = (EditText) view.findViewById(R.id.amount);
        subject = (EditText) view.findViewById(R.id.subject);
        tags = (EditText) view.findViewById(R.id.tags);
        
        amount.setText(transaction.amount.toString());
        subject.setText(transaction.subject);
        tags.setText(transaction.subject);
        
        return builder.create();
    }

}
