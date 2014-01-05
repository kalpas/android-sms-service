package kalpas.expensetracker.view;

import java.util.Comparator;
import java.util.List;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.utils.DateTimeUtil;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(resource, null);

        Transaction tx = items.get(position);
        if (tx != null) {
            TextView amount = (TextView) itemView.findViewById(R.id.amount);
            TextView description = (TextView) itemView.findViewById(R.id.description);
            TextView recipient = (TextView) itemView.findViewById(R.id.recipient);
            TextView date = (TextView) itemView.findViewById(R.id.date);
            View colorLabel = itemView.findViewById(R.id.color_label);

            date.setText(DateTimeUtil.toString(new DateTime(tx.date)));

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
            }

            if (!StringUtils.isEmpty(tx.recipient)) {
                recipient.setText(tx.recipient);
            }

            if (highlight) {
                if (tx.tags != null && tx.tags.contains("cash")) {
                    colorLabel.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.cash_highlight));
                } else {
                    colorLabel.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
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
