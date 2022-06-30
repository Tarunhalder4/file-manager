package com.example.filemanagers.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagers.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.util.List;

public class PathAdapter extends AbstractItem<PathAdapter,PathAdapter.ViewHolder> {

    public File file;

    public PathAdapter(File file) {
        this.file = file;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.each_path_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.each_path;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.folderName.setText(file.getName());
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.folderName.setText(null);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView folderName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.path_name);
        }
    }


}
