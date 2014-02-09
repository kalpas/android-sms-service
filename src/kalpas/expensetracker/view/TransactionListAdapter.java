package kalpas.expensetracker.view;

import static kalpas.expensetracker.view.utils.DateTimeFormatHolder.dateTimeMid;

import java.util.Comparator;
import java.util.List;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.core.Transaction.TranType;
import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;

public class TransactionListAdapter extends ArrayAdapter<Transaction> {

    public static final String      SORT_TYPE_DATE_ASC      = "date asc";
    public static final String      SORT_TYPE_DATE_DESC     = "date desc";
    public static final String      SORT_TYPE_AMOUNT_ASC    = "amount asc";
    public static final String      SORT_TYPE_AMOUNT_DESC   = "amount desc";

    private static final String     KEY_PREF_HIGHLIGHT_CASH = "pref_highlight_cash";

    private final Context           context;
    private final List<Transaction> items;
    private final int               resource;
    private Boolean                 highlight;

    public TransactionListAdapter(Context context, int resource, List<Transaction> objects) {
        super(context, resource, objects);
        this.context = context;
        this.items = objects;
        this.resource = resource;

        highlight = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_PREF_HIGHLIGHT_CASH, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View itemView;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            itemView = inflater.inflate(resource, null);
        } else {
            itemView = convertView;
        }

        Transaction tx = items.get(position);
        if (tx != null) {
            TextView amount = (TextView) itemView.findViewById(R.id.amount);
            TextView description = (TextView) itemView.findViewById(R.id.description);
            TextView recipient = (TextView) itemView.findViewById(R.id.recipient);
            TextView date = (TextView) itemView.findViewById(R.id.date);
            View colorLabel = itemView.findViewById(R.id.color_label);

            date.setText(dateTimeMid.print(tx.date));

            String txAmountString = tx.amount.toString();
            if (TranType.INCOME.equals(tx.type)) {
                txAmountString = "+" + txAmountString;
                amount.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
            } else if (TranType.EXPENSE.equals(tx.type)) {
                txAmountString = "-" + txAmountString;
                amount.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
            } else {
                amount.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
            }

            amount.setText(txAmountString);

            if (!Strings.isNullOrEmpty(tx.description)) {
                description.setText(tx.description);
            } else {
                description.setText(getContext().getResources().getString(R.string.not_specified));
            }

            if (!Strings.isNullOrEmpty(tx.recipient)) {
                recipient.setText(tx.recipient);
            } else {
                recipient.setText(getContext().getResources().getString(R.string.not_specified));
            }

            if (highlight) {
                if (tx.tags != null && tx.tags.contains("cash")) {
                    colorLabel.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.highlight));
                } else {
                    colorLabel.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
                    itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            }

        }

        return itemView;
    }

    public void sort(String type) {
        final String sortType = type;
        super.sort(new Comparator<Transaction>() {
            @Override
            public int compare(Transaction lhs, Transaction rhs) {
                if (SORT_TYPE_DATE_ASC.equals(sortType)) {
                    return compareByDate(lhs, rhs);
                } else if (SORT_TYPE_DATE_DESC.equals(sortType)) {
                    return compareByDate(rhs, lhs);
                } else if (SORT_TYPE_AMOUNT_ASC.equals(sortType)) {
                    return Doubles.compare(Math.abs(lhs.amount), Math.abs(rhs.amount));
                } else if (SORT_TYPE_AMOUNT_DESC.equals(sortType)) {
                    return Doubles.compare(Math.abs(rhs.amount), Math.abs(lhs.amount));
                } else {
                    return 0;
                }
            }

            private int compareByDate(Transaction lhs, Transaction rhs) {
                int result = lhs.date.compareTo(rhs.date);
                if (result == 0) {
                    result = Longs.compare(lhs.id, rhs.id);
                }
                return result;
            }
        });
    }
}
