package kalpas.expensetracker.view.transaction.edit.tags;

import java.util.ArrayList;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Tags;
import kalpas.expensetracker.core.Transaction;
import kalpas.expensetracker.view.TagsListAdapter;
import kalpas.expensetracker.view.TagsListAdapter.Entry;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Joiner;

/**
 * 
 */
public class TagSelectionFragment extends Fragment implements OnClickListener, OnItemClickListener {
    private static final String    ARG_TRANSACTION = "ARG_TRANSACTION";
    public static final String     TAG             = "kalpas.expensetracker.view.transaction.edit.tags.TagSelectionFragment";

    private Transaction            mTransaction;

    private ListView               mTagListView;
    private TagsListAdapter        mTagListAdapter;

    private Button                 okButton;
    private Button                 cancelButton;

    private TextView               preview;

    private OnTagsSelectedListener mEditTranActivity;

    /**
     * Use this factory method to create a new instance of this fragment using
     * the provided parameters.
     * 
     * @param trx
     * @return A new instance of fragment TagSelectionFragment.
     */
    public static TagSelectionFragment newInstance(Transaction transaction) {
        TagSelectionFragment fragment = new TagSelectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSACTION, transaction);
        fragment.setArguments(args);
        return fragment;
    }

    public TagSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTransaction = (Transaction) getArguments().getSerializable(ARG_TRANSACTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tag_selection, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mTagListView = (ListView) getView().findViewById(R.id.tag_list);
        preview = (TextView) getView().findViewById(R.id.tag_preview);

        okButton = (Button) getView().findViewById(R.id.button_save);
        cancelButton = (Button) getView().findViewById(R.id.button_cancel);
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        Tags tagProvider = Tags.getInstance(getActivity());

        mTagListAdapter = new TagsListAdapter(getActivity(), R.layout.tag_list_entry, R.layout.tag_list_section,
                tagProvider.getSuggestedTags(mTransaction), tagProvider.getPopularTags(), tagProvider.getTags());

        mTagListView.setAdapter(mTagListAdapter);
        mTagListView.setOnItemClickListener(this);
        mTagListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    }

    // ************************

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_cancel:
            dismiss();
            break;
        case R.id.button_save:
            acceptTags();
        default:
            break;

        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            acceptTags();
        }

        SparseBooleanArray checked = mTagListView.getCheckedItemPositions();
        ArrayList<String> selectedTags = new ArrayList<String>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i) && checked.keyAt(i) != 0) {
                Entry item = (Entry) mTagListAdapter.getItem(checked.keyAt(i));
                selectedTags.add(item.value);
            }

        }
        preview.setText(Joiner.on(", ").skipNulls().join(selectedTags));

        // Toast.makeText(getActivity(), builder.toString(),
        // Toast.LENGTH_SHORT).show();
    }

    private void dismiss() {
        mEditTranActivity.onDismiss();
    }

    private void acceptTags() {
        SparseBooleanArray checked = mTagListView.getCheckedItemPositions();
        ArrayList<String> selectedTags = new ArrayList<String>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i) && checked.keyAt(i) != 0) {
                Entry item = (Entry) mTagListAdapter.getItem(checked.keyAt(i));
                selectedTags.add(item.value);
            }

        }

        if (!selectedTags.isEmpty()) {
            String oldValue = mTransaction.tags;
            if (!oldValue.isEmpty() && !oldValue.trim().endsWith(",")) {
                oldValue += ", ";
            }
            mTransaction.tags = oldValue + Joiner.on(", ").skipNulls().join(selectedTags) + ",";
        }
        mEditTranActivity.onTagsSelected(mTransaction);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mEditTranActivity = (OnTagsSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTagsSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mEditTranActivity = null;
    }

    public interface OnTagsSelectedListener {
        public void onTagsSelected(Transaction transaction);

        public void onDismiss();
    }

}
