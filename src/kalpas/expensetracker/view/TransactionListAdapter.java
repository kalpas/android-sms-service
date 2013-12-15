package kalpas.expensetracker.view;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TransactionListAdapter extends ArrayAdapter<Transaction> {

    private final Context           context;
    private final List<Transaction> items;
    private final int               resource;

    public TransactionListAdapter(Context context, int resource, List<Transaction> objects) {
        super(context, resource, objects);
        this.context = context;
        this.items = objects;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View itemView = convertView;

        if (itemView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            itemView = inflater.inflate(resource, null);
        }

        Transaction tx = items.get(position);
        if (tx != null) {
            TextView amount = (TextView) itemView.findViewById(R.id.amount);
            TextView description = (TextView) itemView.findViewById(R.id.description);
            TextView recipient = (TextView) itemView.findViewById(R.id.recipient);

            String txAmountString = tx.amount.toString();
            if (tx.amount > 0) {
                txAmountString = "+" + txAmountString;
                amount.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
            } else {
                amount.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
            }
            amount.setText(txAmountString);

            if (!StringUtils.isEmpty(tx.description)) {
                description.setText(tx.description);
            } else {
                description.setText(R.string.not_specified);
            }

            if (!StringUtils.isEmpty(tx.recipient)) {
                recipient.setText(tx.recipient);
            } else {
                recipient.setText(R.string.not_specified);
            }
        }

        return itemView;
    }

}
