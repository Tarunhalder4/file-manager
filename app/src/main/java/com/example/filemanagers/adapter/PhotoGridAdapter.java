package com.example.filemanagers.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagers.BottomSheet;
import com.example.filemanagers.MainActivity;
import com.example.filemanagers.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.util.List;

public class PhotoGridAdapter extends AbstractItem<PhotoGridAdapter,PhotoGridAdapter.ViewHolder> {

    public File file;
    public MainActivity activity;

    public PhotoGridAdapter(MainActivity activity,File file) {
        this.file = file;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.each_photo_row_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.each_photo_row;
    }


    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        if(file.isDirectory()){
            File[] files = file.listFiles();
            assert files != null;
            boolean fileHave = true;
            for (File file: files){
                if(fileHave&&file.getName().endsWith(".jpg")||file.getName().endsWith(".png")){
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    holder.mainFolderImage.setImageBitmap(bitmap);
                    fileHave = false;
                }
            }
        }else {
            if (file.getName().endsWith(".jpg")||file.getName().endsWith(".png")){
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                holder.mainFolderImage.setImageBitmap(bitmap);
            }
        }


        holder.folderName.setText(file.getName());
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheet bottomSheet = new BottomSheet(file);
                bottomSheet.show(activity.getSupportFragmentManager(),"bottom sheet");
            }
        });

    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.folderName.setText(null);
        holder.mainFolderImage.setImageBitmap(null);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mainFolderImage,more;
        TextView folderName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mainFolderImage = itemView.findViewById(R.id.each_photo);
            more = itemView.findViewById(R.id.folder_more);
            folderName =itemView.findViewById(R.id.each_photo_folder_tittle);

        }
    }

}
