package kalpas.expensetracker.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kalpas.expensetracker.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TagsListAdapter extends BaseAdapter {

    private static final int  VIEW_TYPE_SECTION     = 1;
    private static final int  VIEW_TYPE_MANUAL_EDIT = 2;
    private static final int  VIEW_TYPE_COUNT       = 3;

    private final int         mElementLayout        = R.layout.tag_list_entry;
    private final int         mSectionLayout        = R.layout.tag_list_section;
    private final int         mManualEditLayout     = R.layout.tag_list_manual;
    private final Context     mContext;
    private final List<Entry> mItems;

    public TagsListAdapter(Context context, Collection<String> suggested, Collection<String> popular,
            Collection<String> other) {
        this.mContext = context;
        this.mItems = new ArrayList<Entry>();

        // zero item is always 'manual edit'
        Entry manualEdit = new Entry(mContext.getResources().getString(R.string.new_tag));
        mItems.add(manualEdit);

        if (!suggested.isEmpty()) {
            populateSection(popular, R.string.section_suggested);
        } else {
            populateSection(popular, R.string.section_popular);
        }
        
        if (!other.isEmpty()) {
            populateSection(other, R.string.section_other);
        }

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return position == 0 || !mItems.get(position).isSection;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_MANUAL_EDIT;
        } else {
            return mItems.get(position).isSection ? VIEW_TYPE_SECTION : 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int type = getItemViewType(position);

        View itemView = null;
        if (convertView != null) {
            itemView = convertView;
        } else {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            int resource;
            switch (type) {
            case VIEW_TYPE_SECTION:
                resource = mSectionLayout;
                break;
            case VIEW_TYPE_MANUAL_EDIT:
                resource = mManualEditLayout;
                break;
            default:
                resource = mElementLayout;
                break;
            }

            itemView = inflater.inflate(resource, null);
        }

        TextView text = (TextView) itemView.findViewById(R.id.text);
        text.setText(mItems.get(position).value);
        return itemView;

    }

    private void populateSection(Collection<String> other, int captionResource) {
        Entry section = new Entry(mContext.getResources().getString(captionResource));
        section.isSection = true;
        mItems.add(section);
        for (String tag : other) {
            mItems.add(new Entry(tag));
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Entry {
        public String  value;
        public boolean isSection = false;

        public Entry(String value) {
            this.value = value;
        }
    }
}
