package com.example.filemanagers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BottomSheet extends BottomSheetDialogFragment {

    private View copy,delete,move,rename;
    private File file;
    private CircleImageView background;
    private TextView itemName;
    private TextView noOfItemInItem;
    private ImageView folderImageView;

    public BottomSheet(File file) {
        this.file = file;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.bottom_sheet_layout,container,false);

        copy = view.findViewById(R.id.copy_view3);
        delete = view.findViewById(R.id.delete_view24);
        move = view.findViewById(R.id.cut_view2);
        rename = view.findViewById(R.id.rename_view22);


        background = view.findViewById(R.id.profile_image);
        itemName=view.findViewById(R.id.folder_tittle);
        noOfItemInItem= view.findViewById(R.id.folder_number_of_item);
        folderImageView = view.findViewById(R.id.bottom_tittle_icon);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap!=null){
            folderImageView.setVisibility(View.GONE);
            background.setImageBitmap(bitmap);
        }

        itemName.setText(file.getName());
        setFolderImage(file);

        if (file.isDirectory()){
            File[] files = file.listFiles();
            assert files != null;
            long numberOfFile = files.length;
            noOfItemInItem.setText(String.valueOf(numberOfFile));
        }else {
            noOfItemInItem.setText(Constant.memory(file.length()));
        }

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CopyActivity.class);
                List<String> files = new ArrayList<>();
                files.add(file.getAbsolutePath());
                JSONArray jsonArray = new JSONArray(files);
                intent.putExtra(Constant.PATH,jsonArray.toString());
                startActivity(intent);
                Constant.MOVE = false;
                dismiss();
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(file.delete()){
                    String FilePath=file.getParent();
                    EventMessage eventMessage = new EventMessage();
                    eventMessage.setFileDelete(true);
                    eventMessage.setFilePath(FilePath);
                    EventBus.getDefault().post(eventMessage);
                    Toast.makeText(getActivity(), "file is Delete", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        });

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CopyActivity.class);
                List<String> files = new ArrayList<>();
                files.add(file.getAbsolutePath());
                JSONArray jsonArray = new JSONArray(files);
                intent.putExtra(Constant.PATH,jsonArray.toString());
                startActivity(intent);
                Constant.MOVE = true;
                dismiss();
            }
        });


        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRename(getContext());
                dismiss();
            }
        });

        return view;
    }


    void setFolderImage(File file){
        if(file.getName().endsWith(".pdf")){
            folderImageView.setVisibility(View.VISIBLE);
            folderImageView.setImageResource(R.drawable.ic_baseline_picture_as_pdf_24);
            background.setCircleBackgroundColor(getResources().getColor(R.color.red));
        }else if(file.getName().endsWith(".apk")){
            folderImageView.setImageResource(R.drawable.android_8);
            background.setCircleBackgroundColor(getResources().getColor(R.color.yellow));
        }else if (file.getName().endsWith(".mp4")){
            Bitmap bmThumbnail;
            bmThumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
            background.setImageBitmap(bmThumbnail);
            folderImageView.setVisibility(View.GONE);
        }else if (file.getName().endsWith(".mp3")){
            folderImageView.setVisibility(View.VISIBLE);
            folderImageView.setImageResource(R.drawable.music_8);
            background.setCircleBackgroundColor(getResources().getColor(R.color.pink));
        }
    }


    private void setRename(Context context){
        EditText fileName;
        TextView save ,cancel;

        AlertDialog.Builder builder
                = new AlertDialog.Builder(context);
        final View customLayout
                = getLayoutInflater()
                .inflate(R.layout.rename_dialog_box, null);
        builder.setView(customLayout);

        fileName = customLayout.findViewById(R.id.rename_edit_box);
        save = customLayout.findViewById(R.id.rename_save);
        cancel = customLayout.findViewById(R.id.rename_cancel);

        fileName.setText(file.getName());

        AlertDialog dialog = builder.create();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File destination;
                String newName = fileName.getText().toString().trim();
                if (!file.isDirectory()) {
                    String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                    destination = new File(file.getAbsolutePath().replace(file.getName(), newName) + extension);
                } else {
                    destination = new File(file.getAbsolutePath().replace(file.getName(), newName));
                }
                File current = new File(file.getAbsolutePath());
                if (current.renameTo(destination)) {
                    String FilePath=current.getParent();
                    EventMessage eventMessage = new EventMessage();
                    eventMessage.setFileRename(true);
                    eventMessage.setFilePath(FilePath);
                    EventBus.getDefault().post(eventMessage);
                    Toast.makeText(context, "Renamed!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Couldn't Rename!", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }


}
