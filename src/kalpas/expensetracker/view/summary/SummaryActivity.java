package kalpas.expensetracker.view.summary;

import java.util.List;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Core;
import kalpas.expensetracker.core.CoreFactory;
import kalpas.expensetracker.core.Tags;
import kalpas.expensetracker.core.Transaction;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class SummaryActivity extends Activity {

    private TextView stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.summary, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        stats = (TextView) findViewById(R.id.stats);

        Core core = CoreFactory.getInstance(this);
        stats.setText(core.getStats());

        //FIXME temporary solution to keep tags up to date
        Tags tags = Tags.getTagsProvider();
        List<Transaction> list = core.getTransactions();
        for (Transaction trx : list) {
            tags.addTags(trx.tags, this);
        }

    }

}
