package com.example.myswipelist;

import java.util.ArrayList;
import java.util.List;

import com.example.myswiplelist.data.AttachmentModel;
import com.example.myswiplelist.data.AttachmentModel.ATTACHMENT_TYPE;
import com.example.myswiplelist.util.DummyDataUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class MyListAdapter extends BaseAdapter {

    private List<AttachmentModel> mItems = new ArrayList<AttachmentModel>();
    private Activity activity;
    private static LayoutInflater inflater=null;
    private Drawable tagged;
    
    public MyListAdapter(Activity parent){
    	activity = parent;
    	inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	tagged = parent.getResources().getDrawable(R.drawable.tagged_bg);
    	
    }
    
    void resetItems() {
        mItems.clear();
        mItems.addAll(DummyDataUtil.prepareData());
        notifyDataSetChanged();
    }

    

	public void remove(int position) {
        mItems.remove(position);
        notifyDataSetChanged();
    }

    public void insert(int position, AttachmentModel item) {
        mItems.add(position, item);
        notifyDataSetChanged();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mItems.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
    * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @SuppressLint("NewApi")
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	if(position == 0){
    		//return inflater.inflate(R.layout.list_filter, parent, false);
    	}
        ViewHolder holder;
        if(convertView == null) {
        	convertView = inflater.inflate(R.layout.list_item, parent, false);
        	holder = new ViewHolder();
            assert convertView != null;
            holder.senderName = (TextView) convertView.findViewById(R.id.senderName);
            holder.fileName = (TextView) convertView.findViewById(R.id.fileName);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.file_type = (ImageView) convertView.findViewById(R.id.file_type);
            holder.tag = (ImageView) convertView.findViewById(R.id.tag);  
            		
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
      //  position = position - 1;
        holder.position = position;
        holder.senderName.setText(mItems.get(position).getSenderName());
        holder.fileName.setText(mItems.get(position).getAttchFileName());
        holder.date.setText(mItems.get(position).getDate());
        holder.file_type.setImageResource(detectType(mItems.get(position).getAttchType()));
        if(mItems.get(position).getTagName() != ""){
        	holder.tag.setBackground(tagged);
        }
        
        return convertView;
    }

    private int detectType(ATTACHMENT_TYPE attchType) {
		int id = R.drawable.no_image;
		
    	switch (attchType) {
			case ARCHIVE : 
				id = R.drawable.file_type_archive;
				break;
			case AUDIO : 
				id = R.drawable.file_type_audio;
				break;
			case DOC : 
				id = R.drawable.file_type_doc;
				break;
			case DRAWING : 
				id = R.drawable.file_type_drawing;
				break;
			case EXCEL : 
				id = R.drawable.file_type_excel;
				break;
			case TEXT : 
				id = R.drawable.file_type_file;
				break;
			case IMAGE : 
				id = R.drawable.file_type_image;
				break;
			case PDF : 
				id = R.drawable.file_type_pdf;
				break;
			case POWERPOINT : 
				id = R.drawable.file_type_powerpoint;
				break;
			case VIDEO : 
				id = R.drawable.file_type_video;
				break;
			case WORD : 
				id = R.drawable.file_type_word;
				break;
			default :
				id = R.drawable.file_type_fusion;
		
		}
    	return id;
    	
	}

	private class ViewHolder {
        TextView senderName;
        TextView fileName;
        TextView date;
        ImageView file_type;
        ImageView tag;
        int position;
    }

}
