package com.example.filemanagers;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.example.filemanagers.databinding.ActivityCopyBinding;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CopyActivity extends AppCompatActivity {
    ActivityCopyBinding binding;
    private FastItemAdapter<FileAndFolderAdapter> fastItemAdapter;
    ArrayList<File> newFiles;
    String path;
    int backCount =0;
    String destinationPath = null;
    String sourcePath= null;
    String TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCopyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);
        newFiles = new ArrayList<>();

        File file = Environment.getExternalStorageDirectory();
        showFileAndFolder(file,true);

        sourcePath = getIntent().getStringExtra(Constant.PATH);

        fastItemAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {

                if(item.fileAndFolder.isDirectory()){
                    path = item.fileAndFolder.getPath();
                    File file = new File(path);
                    destinationPath = file.getAbsolutePath();
                    Log.d("tag", "onClick: "+destinationPath);
                    showFileAndFolder(file,true);
                }
                return true;
            }
        });

        binding.pastImage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                File sourceFile = new File(sourcePath);
                Log.d("tag", "onClick: "+destinationPath);
                File destinationFile = new File(destinationPath);
                try {
                    Log.d(TAG, "onClick1: file copy");
                    Log.d(TAG, "source path: "+sourcePath);
                    Log.d(TAG, "destination path: "+destinationPath);
                    Files.copy(Paths.get(sourcePath), Paths.get(destinationPath+"/"+sourceFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                    Log.d(TAG, "onClick2: file copy");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onClick: "+e);
                }
                showFileAndFolder(destinationFile,true);
                binding.copyToolBarBottom.setVisibility(View.GONE);
                binding.copyTextView.setText("File Copy");
            }
        });

        binding.cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


       }

    private void showFileAndFolder( File mainFile, boolean fileAndFolder){
        fastItemAdapter.clear();
        List<File> filesAndFolders = Arrays.asList(Objects.requireNonNull(mainFile.listFiles()));
        if (filesAndFolders.size() == 0){
            binding.noFileAvailable.setVisibility(View.VISIBLE);
        }else {
            binding.noFileAvailable.setVisibility(View.GONE);
            List<FileAndFolderAdapter> fileAndFolderAdapters = new ArrayList<>();

            for (File file : filesAndFolders){
                if(file.isDirectory()){
                    fileAndFolderAdapters.add(new FileAndFolderAdapter(file,CopyActivity.this,null));
                }
            }

            if (fileAndFolder){
                for (File file:filesAndFolders){
                    if (file.isFile()){
                        fileAndFolderAdapters.add(new FileAndFolderAdapter(file,CopyActivity.this,null));
                    }
                }
            }
            fastItemAdapter.add(fileAndFolderAdapters);
            binding.copyRecActivity.setLayoutManager(new LinearLayoutManager(CopyActivity.this));
            binding.copyRecActivity.setAdapter(fastItemAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        if(backCount==0){
            destinationPath = path;
        }

        File parent = new File(destinationPath);
        parent=parent.getParentFile();
        destinationPath=parent.getAbsolutePath();
        fastItemAdapter.clear();
        showFileAndFolder(parent,true);
        backCount=1;

        if(destinationPath.equals("/storage/emulated/0")){
            super.onBackPressed();
        }
    }

}


