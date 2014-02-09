package kalpas.expensetracker.view.transaction.edit.tags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Tags;
import kalpas.expensetracker.core.Transaction;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 * 
 */
public class TagSelectionFragment extends Fragment implements OnClickListener {
    private static final String    ARG_TRANSACTION = "ARG_TRANSACTION";
    public static final String     TAG             = "kalpas.expensetracker.view.transaction.edit.tags.TagSelectionFragment";

    private Transaction            mTransaction;

    private LinearLayout           tagListView;

    private Button                 okButton;
    private Button                 cancelButton;

    private TextView               preview;

    private OnTagsSelectedListener hostingActivity;

    private List<String>           toggledTags     = new ArrayList<String>();

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

        tagListView = (LinearLayout) getView().findViewById(R.id.tag_list);
        preview = (TextView) getView().findViewById(R.id.tag_preview);

        okButton = (Button) getView().findViewById(R.id.button_save);
        cancelButton = (Button) getView().findViewById(R.id.button_cancel);
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        Tags tagProvider = Tags.getInstance(getActivity());

        tagListView.addView(createNewTagButton());

        Collection<String> suggestedTags = tagProvider.getSuggestedTags(mTransaction);
        if (!suggestedTags.isEmpty()) {
            tagListView.addView(createSection(R.string.section_suggested));
            for (String tag : suggestedTags) {
                // tagsContainerLayout.addView(createDivider());
                tagListView.addView(createTagButton(tag));
            }
        } else {
            suggestedTags = tagProvider.getPopularTags();
            tagListView.addView(createSection(R.string.section_popular));
            for (String tag : suggestedTags) {
                // tagsContainerLayout.addView(createDivider());
                tagListView.addView(createTagButton(tag));
            }
        }

        tagListView.addView(createSection(R.string.section_other));

        Collection<String> tagsList = tagProvider.getTags();
        for (String tag : tagsList) {
            // tagsContainerLayout.addView(createDivider());
            tagListView.addView(createTagButton(tag));
        }
    }

    // ************************

    private TextView createNewTagButton() {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources()
                .getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = getTagListLayoutParams();

        Button newTag = new Button(getActivity(), null, android.R.attr.buttonBarButtonStyle);
        newTag.setLayoutParams(layoutParams);
        newTag.setTypeface(null, Typeface.ITALIC);
        newTag.setText(getResources().getString(R.string.new_tag));
        newTag.setClickable(true);
        newTag.setMinimumHeight(height);
        newTag.setMinHeight(height);
        newTag.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        newTag.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                acceptTags();
            }
        });
        return newTag;
    }

    private TextView createSection(int resource) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources()
                .getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = getTagListLayoutParams();

        TextView section = new TextView(getActivity(), null, android.R.attr.buttonBarButtonStyle);
        section.setLayoutParams(layoutParams);
        section.setTypeface(null, Typeface.BOLD);
        section.setBackgroundColor(getResources().getColor(R.color.section));
        section.setText(getResources().getString(resource));
        section.setMinimumHeight(height);
        section.setMinHeight(height);
        section.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        return section;
    }

    private ToggleButton createTagButton(String tag) {
        ToggleButton itemView;
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources()
                .getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = getTagListLayoutParams();

        itemView = new ToggleButton(getActivity(), null, android.R.attr.buttonBarButtonStyle);
        itemView.setLayoutParams(layoutParams);
        itemView.setMinimumHeight(height);
        itemView.setMinHeight(height);
        itemView.setText(tag);
        itemView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton button = (ToggleButton) v;
                if (button.isChecked()) {
                    toggledTags.add(button.getText().toString());
                    button.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
                    button.setBackgroundColor(getResources().getColor(R.color.highlight));
                } else {
                    toggledTags.remove(button.getText().toString());
                    button.setTextColor(getResources().getColor(android.R.color.white));
                    button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
                preview.setText(Joiner.on(", ").skipNulls().join(toggledTags) + ",");
            }
        });

        return itemView;
    }

    /**
     * add tagsEditText onClick
     */
    public void onAcceptTagsClick(View v) {
    }

    private LinearLayout.LayoutParams getTagListLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        return layoutParams;
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

    private void dismiss() {
        hostingActivity.onDismiss();
    }

    private void acceptTags() {
        if (!toggledTags.isEmpty()) {
            String oldValue = mTransaction.tags;
            if (!oldValue.isEmpty() && !oldValue.trim().endsWith(",")) {
                oldValue += ", ";
            }
            mTransaction.tags = oldValue + Joiner.on(", ").skipNulls().join(toggledTags) + ",";
            toggledTags.clear();
        }
        hostingActivity.onTagsSelected(mTransaction);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            hostingActivity = (OnTagsSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTagsSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hostingActivity = null;
    }

    public interface OnTagsSelectedListener {
        public void onTagsSelected(Transaction transaction);

        public void onDismiss();
    }

}
