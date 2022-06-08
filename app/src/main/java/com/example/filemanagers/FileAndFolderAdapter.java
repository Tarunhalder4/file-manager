package com.example.filemanagers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileAndFolderAdapter extends AbstractItem<FileAndFolderAdapter, FileAndFolderAdapter.ViewHolder> {

    File fileAndFolder;

    public FileAndFolderAdapter(File fileAndFolder) {
        this.fileAndFolder = fileAndFolder;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.each_row_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.each_row;
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<FileAndFolderAdapter>{
        ImageView fileAndFolderImage;
        TextView fileAndFolderName, numberOfItem;
        public ViewHolder(View itemView) {
            super(itemView);
            fileAndFolderImage = itemView.findViewById(R.id.file_and_folder_image);
            fileAndFolderName = itemView.findViewById(R.id.file_and_folder_name);
            numberOfItem = itemView.findViewById(R.id.number_of_item);
        }

        @Override
        public void bindView(FileAndFolderAdapter item, List<Object> payloads) {
            if (item.fileAndFolder.isDirectory()){
                fileAndFolderImage.setImageResource(R.drawable.fill_folder_48);
                List<File> files = Arrays.asList(Objects.requireNonNull(item.fileAndFolder.listFiles()));
                if (files.size()!=0) {
                    numberOfItem.setText(String.valueOf(files.size()));
                }
            }else{
                fileAndFolderImage.setImageResource(R.drawable.file_48);
            }

            fileAndFolderName.setText(item.fileAndFolder.getName());

        }

        @Override
        public void unbindView(FileAndFolderAdapter item) {
            fileAndFolderName.setText(null);
            fileAndFolderImage.setImageResource(0);
            numberOfItem.setText(null);
        }

    }
}
