package com.example.myswipelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by beckchen on 1/30/15.
 */
public class TagDialogAdapter extends BaseAdapter {
    private Context context;
    private List<String> recentTags;
    public TagDialogAdapter(Context context, List<String> recentTags) {
        this.context = context;
        this.recentTags = recentTags;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.recent_tag_dialog_item, null);

            // set value into textview
            TextView textView = (TextView) gridView
                    .findViewById(R.id.recentTagTextView);
            textView.setText(recentTags.get(position));

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return recentTags.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}
