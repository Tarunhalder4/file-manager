package com.example.filemanagers.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagers.Constant;
import com.example.filemanagers.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class BookmarkAdapter extends AbstractItem<BookmarkAdapter, BookmarkAdapter.ViewHolder>{
    int folderImage;
    String folderName;
    int layoutType;

    public BookmarkAdapter(int folderImage, String folderName, int layoutType) {
        this.folderImage = folderImage;
        this.folderName = folderName;
        this.layoutType = layoutType;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.bookmark_item_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.bookmark_item;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        if(layoutType== Constant.CONTAINS_INTERNAL_STORAGE){
            holder.moreImage.setVisibility(View.INVISIBLE);
            holder.folderImage.setImageResource(folderImage);
            holder.folderName.setText(folderName);
        }else {
            holder.moreImage.setVisibility(View.VISIBLE);
            holder.folderImage.setImageResource(folderImage);
            holder.folderName.setText(folderName);
        }
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.moreImage.setVisibility(View.GONE);
        holder.folderImage.setImageResource(0);
        holder.folderName.setText(null);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView folderImage, moreImage;
        TextView folderName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderImage = itemView.findViewById(R.id.folder_image);
            folderName = itemView.findViewById(R.id.folder_name);
            moreImage = itemView.findViewById(R.id.more);
        }
    }
}
