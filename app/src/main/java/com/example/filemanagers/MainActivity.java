package com.example.filemanagers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.filemanagers.databinding.ActivityMainBinding;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    String path;
    int backCount =0;
    String destinationPath = null;
    boolean peekPath= false;

    private String TAG = "tag";
    private ActivityMainBinding binding;
    private FastItemAdapter<FileAndFolderAdapter> fastItemAdapter;
    private List<FileAndFolderAdapter> fileAndFolderAdapters;
    ArrayList<File> newFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate: Main activity");

        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);
        newFiles = new ArrayList<>();
        if(Constant.checkPermission(MainActivity.this)){
            formIntentGetData();
        }else {
            Constant.requestPermission(MainActivity.this);
        }

        fastItemAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {

                if(item.fileAndFolder.isDirectory()){
                    path = item.fileAndFolder.getPath();
                    peekPath = true;
                    showFileAndFolder(item.fileAndFolder,false,"all");
                }else {
                    if(item.fileAndFolder.getPath().endsWith(".pdf")){
                        openFile(item.fileAndFolder,Constant.PDF_FILE);
                    }

                    if (item.fileAndFolder.getPath().endsWith(".jpg")){
                        openFile(item.fileAndFolder, Constant.PHOTO_FILE);
                    }

                    if (item.fileAndFolder.getPath().endsWith(".mp3")){
                        Log.d(TAG, "onClick: "+item.fileAndFolder.getPath().endsWith(".mp3"));
                        openFile(item.fileAndFolder,Constant.AUDIO_FILE);
                    }

                    if (item.fileAndFolder.getPath().endsWith(".mp4")){
                        openFile(item.fileAndFolder,Constant.VIDEO_FILE);
                    }

                }
                return true;
            }
        });

    }

    void openFile(File file,String type){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                getApplicationContext().getApplicationContext().getPackageName() +
                        ".provider", file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if(type.equals(Constant.PHOTO_FILE)){
            intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "image/*");
        }

        if(type.equals(Constant.PDF_FILE)){
            intent.setDataAndType(photoURI,"application/pdf");
        }

        if(type.equals(Constant.AUDIO_FILE)){
            intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "audio/*");
        }

        if(type.equals(Constant.VIDEO_FILE)){
            intent.setDataAndType(Uri.parse(file.getAbsolutePath()),"video/*");
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "openFile: "+e);
        }
    }


    private void scanDirectory(File directory) {
         if (directory != null) {
             File[] listFiles = directory.listFiles();
             if (listFiles != null && listFiles.length > 0) {

                 for (File file : listFiles) {
                     if (file.isDirectory()) {
                         scanDirectory(file);
                     } else {
                         newFiles.add(file);
                     }

                 }
             }
         }
    }


    private void formIntentGetData(){
        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.PHOTO_FOLDER)){
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            showFileAndFolder(file, true,"all");
            backCount=-1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DOWNLOAD_FOLDER)){
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            showFileAndFolder(file, true,"all");
            backCount=-1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.SAFE_BOX_FOLDER)){
            Toast.makeText(MainActivity.this,Constant.SAFE_BOX_FOLDER,Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.MUSIC_FOLDER)){
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, true,"audio");
            backCount=-1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.RECENT_FILE)){
            Toast.makeText(MainActivity.this,Constant.RECENT_FILE,Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DOCUMENTS_FOLDER)){
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, true,"doc");
            backCount=-1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.APP_MANAGER_FOLDER)){
            Toast.makeText(MainActivity.this,Constant.APP_MANAGER_FOLDER,Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.VIDEO_FOLDER)){
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            showFileAndFolder(file, true,"all");
            backCount=-1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.AAD_TO_QUICK_ACCESS)){
            Toast.makeText(MainActivity.this,Constant.AAD_TO_QUICK_ACCESS,Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.INTERNAL_STORAGE_PATH)){
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path);
            showFileAndFolder(file,false,"all");
            backCount=1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.FOLDER_PATH)){
            String path = getIntent().getStringExtra(Constant.FOLDER_PATH);
            File file = new File(path);
            showFileAndFolder(file,false,"all");
            backCount=-1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DCIM_FOLDER)){
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            showFileAndFolder(file, false,"all");
            backCount=-1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.PICTURES_FOLDER)){
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            showFileAndFolder(file, false,"all");
            backCount=-1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.MOVIES_FOLDER)){
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            showFileAndFolder(file, false,"all");
            backCount=-1;
        }
    }


    private void showFileAndFolder( File mainFile, boolean onlyfile, String requiredFile){
        fastItemAdapter.clear();
        if(!Constant.checkPermission(MainActivity.this)){
            binding.noFileAvailable.setText("Permission required for display file");
            binding.noFileAvailable.setVisibility(View.VISIBLE);
        }
        List<File> filesAndFolders = Arrays.asList(Objects.requireNonNull(mainFile.listFiles()));
        if (filesAndFolders.size() == 0){
            binding.noFileAvailable.setVisibility(View.VISIBLE);
            Log.d(TAG, "empty folder");
        }else {
            binding.noFileAvailable.setVisibility(View.GONE);
            fileAndFolderAdapters = new ArrayList<>();

            if(requiredFile.equals("audio")){
                for (File file : filesAndFolders){
                    if(file.isDirectory()){
                        scanDirectory(file);
                    }else{
                        for (File file2 : newFiles){
                            if(file2.isFile()){
                                if (file2.getName().endsWith("mp3")){
                                   fileAndFolderAdapters.add(new FileAndFolderAdapter(file2,MainActivity.this,MainActivity.this));
                                }
                            }
                        }
                    }
                }
            }else if(requiredFile.equals("doc")){
                for (File file : filesAndFolders){
                    if(file.isDirectory()){
                        scanDirectory(file);
                    }else{
                        for (File file2 : newFiles){
                            if(file2.isFile()){
                                if (file2.getName().endsWith("pdf")
                                        ||file2.getName().endsWith("txt")
                                        ||file2.getName().endsWith("xlsx")
                                        ||file2.getName().equals("csv")
                                        ||file2.getName().equals("pptx")){
                                    fileAndFolderAdapters.add(new FileAndFolderAdapter(file2,MainActivity.this,MainActivity.this));
                                }
                            }
                        }
                    }
                }
            }else if(requiredFile.equals("all")){
                if(onlyfile){
                    for (File file : filesAndFolders){
                        if(file.isFile()){
                            fileAndFolderAdapters.add(new FileAndFolderAdapter(file,MainActivity.this,MainActivity.this));
                        }
                    }
                }else {
                    for (File file : filesAndFolders){
                        if(file.isDirectory()){
                            fileAndFolderAdapters.add(new FileAndFolderAdapter(file,MainActivity.this,MainActivity.this));
                        }
                    }

                    for (File file : filesAndFolders){
                        if(file.isFile()){
                            fileAndFolderAdapters.add(new FileAndFolderAdapter(file,MainActivity.this,MainActivity.this));
                        }
                    }
                }
            }
            fastItemAdapter.add(fileAndFolderAdapters);
            binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            binding.rec.setAdapter(fastItemAdapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==111){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                formIntentGetData();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed: "+backCount);
        if(backCount==-1){
            backCount=0;
            super.onBackPressed();
        }else if(backCount==0 || peekPath){
            if (peekPath){
                destinationPath = path;
                peekPath = false;
            }
            File parent = new File(destinationPath);
            parent=parent.getParentFile();
            destinationPath=parent.getAbsolutePath();
            fastItemAdapter.clear();
            showFileAndFolder(parent,false,"all");

            if(destinationPath.equals("/storage/emulated/0")){
                backCount=1;
            }else {
                backCount=0;
            }

        }else {
            super.onBackPressed();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage event) {
        if(event.isDelete()){
            String deletePath = event.getDeleteFilePath();
            Log.d(TAG, "onMessageEvent: "+deletePath);
            File file = new File(deletePath);
            showFileAndFolder(file,true,"all");
            Log.d(TAG, "onMessageEvent: ");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}