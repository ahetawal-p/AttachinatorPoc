package com.example.myswipelist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myswiplelist.data.AttachmentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by beckchen on 1/28/15.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>{

    private List<AttachmentModel> mItems;
    public RecyclerViewAdapter(ArrayList<AttachmentModel> mItems) {
        this.mItems = mItems;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item
                , viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder viewHolder, int i) {
        viewHolder.position = i;
        viewHolder.senderName.setText(mItems.get(i).getSenderName());
        viewHolder.fileName.setText(mItems.get(i).getAttchFileName());
        viewHolder.date.setText(mItems.get(i).getDate());
        viewHolder.file_type.setImageResource(detectType(mItems.get(i).getAttchType()));
        if(mItems.get(i).getTagName() != ""){
            //viewHolder.tag.setBackground(tagged);
        }
    }

    @Override
    public int getItemCount() {
        int size = mItems.size();
        try {
            return size;
        }
        catch (Exception ex){
            String asdf = ex.toString();
            return 1;
        }
    }

    public void remove(int position) {
        mItems.remove(position);
        //notifyItemRemoved(position);
    }

    private int detectType(AttachmentModel.ATTACHMENT_TYPE attchType) {
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

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView senderName;
        private TextView fileName;
        private TextView date;
        private ImageView file_type;
        private ImageView tag;
        private int position;

        public CustomViewHolder(View itemView) {
            super(itemView);

            senderName = (TextView) itemView.findViewById(R.id.senderName);
            fileName = (TextView) itemView.findViewById(R.id.fileName);
            date = (TextView) itemView.findViewById(R.id.date);
            file_type = (ImageView) itemView.findViewById(R.id.file_type);
            senderName = (TextView) itemView.findViewById(R.id.senderName);
        }
    }

}



