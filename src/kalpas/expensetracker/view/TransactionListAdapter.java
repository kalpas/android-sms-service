package kalpas.expensetracker.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.utils.DateTimeUtil;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TransactionListAdapter extends ArrayAdapter<Transaction> implements OnSharedPreferenceChangeListener {

    public enum SortTypes {
        date;
    }

    private static final String     KEY_PREF_HIGHLIGHT_CASH = "pref_highlight_cash";

    private final Context           context;
    private final List<Transaction> items;
    private final int               resource;
    private Boolean                 highlight;
    private SortTypes               sort;

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

            if (highlight && tx.tags.contains("cash")) {
                itemView.setBackgroundColor(context.getResources().getColor(R.color.cash_highlight));
            }

        }

        return itemView;
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(items, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction lhs, Transaction rhs) {
                switch (sort) {
                case date:
                    return lhs.date.compareTo(rhs.date);
                default:
                    return lhs.compareTo(rhs);
                }
            }
        });
        super.notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_HIGHLIGHT_CASH)) {
            highlight = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(KEY_PREF_HIGHLIGHT_CASH, true);
        }

    }

}
