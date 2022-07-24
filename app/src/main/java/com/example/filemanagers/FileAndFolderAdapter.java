package com.example.filemanagers;

import android.annotation.SuppressLint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.materialize.util.UIUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FileAndFolderAdapter extends AbstractItem<FileAndFolderAdapter, FileAndFolderAdapter.ViewHolder> {

    File fileAndFolder;
    public String TAG = "tag";
    public Context context;
    public MainActivity mainActivity;
    public FileAndFolderAdapter(File fileAndFolder, Context context,MainActivity mainActivity) {
        this.fileAndFolder = fileAndFolder;
        this.context = context;
        this.mainActivity = mainActivity;
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
        TextView fileAndFolderName, date, fileSize;
        CardView folderIdentifierCardView, fileAndFolderBackGround;
        ImageView folderIdentifierImage, photo, moreImage;
        public ViewHolder(View itemView) {
            super(itemView);
            fileAndFolderImage = itemView.findViewById(R.id.file_and_folder_image);
            fileAndFolderName = itemView.findViewById(R.id.file_and_folder_name);
            date = itemView.findViewById(R.id.date);
            fileSize = itemView.findViewById(R.id.size);
            folderIdentifierImage =itemView.findViewById(R.id.folder_identifier_image);
            folderIdentifierCardView= itemView.findViewById(R.id.folder_identifier_cardView);
            fileAndFolderBackGround = itemView.findViewById(R.id.file_and_folder_back_ground);
            photo = itemView.findViewById(R.id.photos);
            moreImage = itemView.findViewById(R.id.more_image);
        }


        @Override
        public void bindView(FileAndFolderAdapter item, List<Object> payloads) {
            UIUtils.setBackground(itemView, FastAdapterUIUtils.getSelectableBackground(item.context, Color.RED, true));
            if (item.fileAndFolder.isDirectory()){
                fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.folderBackground));

                if (item.fileAndFolder.getName().equals(Environment.DIRECTORY_DCIM)){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.camera_8);
                }else if(item.fileAndFolder.getName().equals(Environment.DIRECTORY_MUSIC)){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.music_8);
                }else if(item.fileAndFolder.getName().equals(Environment.DIRECTORY_MOVIES)){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.movies_8);
                }else if(item.fileAndFolder.getName().equals(Environment.DIRECTORY_DOWNLOADS)){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.file_download_8);
                }else if(item.fileAndFolder.getName().equals(Environment.DIRECTORY_DOCUMENTS)){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.file_8);
                }else if(item.fileAndFolder.getName().equals(Environment.DIRECTORY_PICTURES)){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.photo_library_8);
                }else if(item.fileAndFolder.getName().equals("Android")){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.android_8);
                }else if(item.fileAndFolder.getName().equals(Environment.DIRECTORY_DOCUMENTS)){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.file_8);
                }else if(item.fileAndFolder.getName().equals("WhatsApp")){
                    folderIdentifierCardView.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setVisibility(View.VISIBLE);
                    folderIdentifierImage.setImageResource(R.drawable.whatup_icon);
                }else {
                    folderIdentifierCardView.setVisibility(View.GONE);
                    folderIdentifierImage.setVisibility(View.GONE);
                }

                fileAndFolderImage.setImageResource(R.drawable.folder_open_24);
//                List<File> files = Arrays.asList(Objects.requireNonNull(item.fileAndFolder.listFiles()));
//                if (files.size()!=0) {
//                    fileSize.setText(String.valueOf(files.size()));
//                }
            }else{
                if (item.fileAndFolder.getName().endsWith("txt")){
                    fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.gray1));
                    fileAndFolderImage.setImageResource(R.drawable.file_8);
                }else if(item.fileAndFolder.getName().endsWith("pdf")){
                    fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.red));
                    fileAndFolderImage.setImageResource(R.drawable.ic_baseline_picture_as_pdf_24);
                }else if(item.fileAndFolder.getName().endsWith("jpg")||item.fileAndFolder.getName().endsWith("jpeg")||item.fileAndFolder.getName().endsWith(".png")){
                    photo.setVisibility(View.VISIBLE);
                    fileAndFolderImage.setVisibility(View.GONE);
                    Bitmap bitmap = BitmapFactory.decodeFile(item.fileAndFolder.getAbsolutePath());
                    if (bitmap!=null){
                        photo.setImageBitmap(bitmap);
                    }else {
                        fileAndFolderImage.setVisibility(View.VISIBLE);
                        fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.blue));
                        fileAndFolderImage.setImageResource(R.drawable.photo_library_8);
                    }
                }else if(item.fileAndFolder.getName().endsWith("zip")){
                    fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.gray1));
                    fileAndFolderImage.setImageResource(R.drawable.folder_zip_24);
                }else if(item.fileAndFolder.getName().endsWith("apk")){
                    fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.yellow));
                    fileAndFolderImage.setImageResource(R.drawable.android_8);
                }else if(item.fileAndFolder.getName().endsWith("mp3")){
                    fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.pink));
                    fileAndFolderImage.setImageResource(R.drawable.music_8);
                }else if(item.fileAndFolder.getName().endsWith("opus")){
                    fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.pink));
                    fileAndFolderImage.setImageResource(R.drawable.music_8);
                }else if(item.fileAndFolder.getName().endsWith("xlsx")){
                    fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.dark_green));
                    fileAndFolderImage.setImageResource(R.drawable.xlsx_24);
                }else if(item.fileAndFolder.getName().endsWith(".mp4")){
                    Bitmap bmThumbnail;
                    bmThumbnail = ThumbnailUtils.createVideoThumbnail(item.fileAndFolder.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                    photo.setVisibility(View.VISIBLE);
                    photo.setImageBitmap(bmThumbnail);
                    fileAndFolderImage.setVisibility(View.GONE);
                }else {
                    fileAndFolderBackGround.setCardBackgroundColor(item.context.getResources().getColor(R.color.neon_blue));
                    fileAndFolderImage.setImageResource(R.drawable.red_file_24);
                }

                folderIdentifierCardView.setVisibility(View.GONE);
                folderIdentifierImage.setVisibility(View.GONE);
            }

            if (item.mainActivity==null){
                moreImage.setVisibility(View.GONE);
            }else{
                moreImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moreImage.setVisibility(View.VISIBLE);
                        BottomSheet bottomSheet = new BottomSheet(item.fileAndFolder);
                        bottomSheet.show(item.mainActivity.getSupportFragmentManager(),"bottom sheet");
                    }
                });
            }

            long lastModifiedDate = item.fileAndFolder.lastModified();
            Date modifiedDate = new Date(lastModifiedDate);
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            date.setText(sdf.format(modifiedDate));

            fileAndFolderName.setText(item.fileAndFolder.getName());

        }

        @Override
        public void unbindView(FileAndFolderAdapter item) {

//            ImageView fileAndFolderImage;
//            TextView fileAndFolderName, date, fileSize;
//            CardView folderIdentifierCardView, fileAndFolderBackGround;
//            ImageView folderIdentifierImage, photo, moreImage;


            fileAndFolderImage.setImageResource(0);
            fileAndFolderName.setText(null);
            fileAndFolderBackGround.setCardBackgroundColor(null);
            date.setText(null);
            fileSize.setText(null);
            photo.setImageResource(0);
            folderIdentifierCardView.setVisibility(View.GONE);


        }

    }
}
