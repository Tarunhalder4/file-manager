package com.example.filemanagers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filemanagers.databinding.ActivityMainBinding;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private String TAG = "tag";
    private ActivityMainBinding binding;
    private FastItemAdapter<FileAndFolderAdapter> fastItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);

        if(Constant.checkPermission(MainActivity.this)){
            if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DOWNLOAD_FOLDER)){
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                showFileAndFolder(file, true);
            }

            if (getIntent().getStringExtra(Constant.PATH).equals(Constant.MUSIC_FOLDER)){
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                showFileAndFolder(file, true);
            }

            if (getIntent().getStringExtra(Constant.PATH).equals(Constant.INTERNAL_STORAGE_PATH)){
                String path = Environment.getExternalStorageDirectory().toString();
                Log.d("Files", "Path: " + path);
                File file = new File(path);
                showFileAndFolder(file,false);
            }

            if (getIntent().getStringExtra(Constant.PATH).equals(Constant.FOLDER_PATH)){
                String path = getIntent().getStringExtra(Constant.FOLDER_PATH);
                File file = new File(path);
                showFileAndFolder(file,false);
            }

        }else {
            Constant.requestPermission(MainActivity.this);
        }


        fastItemAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {

                if(item.fileAndFolder.isDirectory()){
                    String path = item.fileAndFolder.getPath();
                    Log.d(TAG, "onClick: "+path);
                    Intent intent = new Intent(MainActivity.this,MainActivity.class);
                    intent.putExtra(Constant.PATH,Constant.FOLDER_PATH);
                    intent.putExtra(Constant.FOLDER_PATH,path);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return true;
            }
        });

    }


    private void showFileAndFolder( File mainFile, boolean onlyfile){
        List<File> filesAndFolders = Arrays.asList(Objects.requireNonNull(mainFile.listFiles()));
        if (filesAndFolders.size() == 0){
            binding.noFileAvailable.setVisibility(View.VISIBLE);
            Log.d(TAG, "empty folder");
        }else {
            binding.noFileAvailable.setVisibility(View.GONE);
            Log.d(TAG, "onClick1: "+filesAndFolders.size());
            List<FileAndFolderAdapter> fileAndFolderAdapters = new ArrayList<>();
            if(onlyfile){
                for (File file : filesAndFolders){
                    if(file.isFile()){
                        fileAndFolderAdapters.add(new FileAndFolderAdapter(file));
                    }
                }
            }else {
                for (File file : filesAndFolders){
                    fileAndFolderAdapters.add(new FileAndFolderAdapter(file));
                }
            }

            fastItemAdapter.add(fileAndFolderAdapters);
            binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            binding.rec.setAdapter(fastItemAdapter);
        }
    }

}