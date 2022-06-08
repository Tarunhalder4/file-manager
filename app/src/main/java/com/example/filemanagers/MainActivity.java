package com.example.filemanagers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
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

        if (Constant.flag){
            if(checkPermission()){
                String path = Environment.getExternalStorageDirectory().getPath();
                setDataInRec(path);
                Constant.flag = false;
            }else {
                requestPermission();
            }
        }

        if(getIntent().getStringExtra("path")!=null){
            String path = getIntent().getStringExtra("path");
            setDataInRec(path);
        }

        fastItemAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {

                if(item.fileAndFolder.isDirectory()){
                    String path = item.fileAndFolder.getPath();
                    Intent intent = new Intent(MainActivity.this,MainActivity.class);
                    intent.putExtra("path",path);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return true;
            }
        });

    }


    private void setDataInRec(String path){
        File root = new File(path);
        List<File> filesAndFolders = Arrays.asList(Objects.requireNonNull(root.listFiles()));
        if (filesAndFolders.size() == 0){
            binding.noFileAvailable.setVisibility(View.VISIBLE);
            Log.d(TAG, "empty folder");
        }else {
            binding.noFileAvailable.setVisibility(View.GONE);
            Log.d(TAG, "onClick1: "+filesAndFolders.size());
            List<FileAndFolderAdapter> fileAndFolderAdapters = new ArrayList<>();
            for (File file : filesAndFolders){
                fileAndFolderAdapters.add(new FileAndFolderAdapter(file));
            }
            fastItemAdapter.add(fileAndFolderAdapters);
            binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            binding.rec.setAdapter(fastItemAdapter);
        }
    }


    private void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(this, "Storage permission are required", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},111);
    }

    private boolean checkPermission(){
        int writePermission = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE);
        if (writePermission==PackageManager.PERMISSION_GRANTED && readPermission==PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
            return true;
        }else {
            return false;
        }
    }

}