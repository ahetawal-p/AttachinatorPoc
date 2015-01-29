package com.example.myswipelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myswiplelist.data.DeferDialogModel;

import java.util.List;

/**
 * Created by beckchen on 1/28/15.
 */
public class DeferDialogAdapter extends BaseAdapter {
    private Context context;
    private List<DeferDialogModel> dialogItems;

    public DeferDialogAdapter(Context context, List<DeferDialogModel> dialogItems) {
        this.context = context;
        this.dialogItems = dialogItems;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.defer_dialog_item, null);

            // set value into textview
            TextView textView = (TextView) gridView
                    .findViewById(R.id.grid_item_label);
            textView.setText(dialogItems.get(position).getItemLabel());

            // set image based on selected text
            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_item_image);

            int itemImageResource = dialogItems.get(position).getItemImageResourceId();
            if (itemImageResource == 0){

            }            else
                imageView.setImageResource(dialogItems.get(position).getItemImageResourceId());


        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return dialogItems.size();
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