package com.example.filemanagers.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagers.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class PieAdapter extends AbstractItem<PieAdapter, PieAdapter.ViewHolder> {
    int dotImage;
    String fileType,fileSize;

    public PieAdapter(int dotImage, String fileType, String fileSize) {
        this.dotImage = dotImage;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.pie_chat_item_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.pie_chat_item;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.dotImage.setImageResource(dotImage);
        holder.fileType.setText(fileType);
        holder.fileSize.setText(fileSize +"MB");
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.dotImage.setImageResource(0);
        holder.fileType.setText(null);
        holder.fileSize.setText(null);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView dotImage;
        TextView fileType, fileSize;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dotImage = itemView.findViewById(R.id.dot_image);
            fileType =itemView.findViewById(R.id.file_type);
            fileSize = itemView.findViewById(R.id.file_size);
        }
    }
}
