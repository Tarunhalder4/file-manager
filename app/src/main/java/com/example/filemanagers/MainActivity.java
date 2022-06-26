package com.example.filemanagers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
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

import com.example.filemanagers.adapter.PhotoGridAdapter;
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
    int backCount = 0;
    String destinationPath = null;
    boolean peekPath = false;

    private String TAG = "tag";
    private ActivityMainBinding binding;
    private FastItemAdapter<FileAndFolderAdapter> fileAndFolderAdapterFastItemAdapter;
    private FastItemAdapter<PhotoGridAdapter> photoGridAdapterFastItemAdapter;
    private List<FileAndFolderAdapter> fileAndFolderAdapters;
    private List<PhotoGridAdapter> photoGridAdapters;
    ArrayList<File> newFiles = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate: Main activity");

        fileAndFolderAdapterFastItemAdapter = new FastItemAdapter<>();
        fileAndFolderAdapterFastItemAdapter.withSelectable(true);

        photoGridAdapterFastItemAdapter = new FastItemAdapter<>();
        photoGridAdapterFastItemAdapter.withSelectable(true);
        photoGridAdapters = new ArrayList<>();

        newFiles = new ArrayList<>();
        if (Constant.checkPermission(MainActivity.this)) {
            formIntentGetData();
        } else {
            Constant.requestPermission(MainActivity.this);
        }

        photoGridAdapterFastItemAdapter.withOnClickListener(new OnClickListener<PhotoGridAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<PhotoGridAdapter> adapter, PhotoGridAdapter item, int position) {

                Log.d(TAG, "onClick: "+item.file.getName());
                if(item.file.isDirectory()){
                    photoGridAdapterFastItemAdapter.clear();
                    showPhotoInFolder(item.file);
                }else {
                    if(item.file.isFile() && item.file.getName().endsWith("jpg") ){
                        openFile(item.file,Constant.PHOTO_FILE);
                    }
                }
                return true;
            }
        });

        fileAndFolderAdapterFastItemAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {

                if (item.fileAndFolder.isDirectory()) {
                    binding.noFileAvailable.setVisibility(View.GONE);
                    path = item.fileAndFolder.getPath();
                    peekPath = true;
                    showFileAndFolder(item.fileAndFolder, Constant.INTERNAL_STORAGE_FILE_FOLDER);
                } else {
                    if (item.fileAndFolder.getPath().endsWith(".pdf")) {
                        openFile(item.fileAndFolder, Constant.PDF_FILE);
                    }

                    if (item.fileAndFolder.getPath().endsWith(".jpg")) {
                        openFile(item.fileAndFolder, Constant.PHOTO_FILE);
                    }

                    if (item.fileAndFolder.getPath().endsWith(".mp3")) {
                        Log.d(TAG, "onClick: " + item.fileAndFolder.getPath().endsWith(".mp3"));
                        openFile(item.fileAndFolder, Constant.AUDIO_FILE);
                    }

                    if (item.fileAndFolder.getPath().endsWith(".mp4")) {
                        openFile(item.fileAndFolder, Constant.VIDEO_FILE);
                    }

                    if (item.fileAndFolder.isDirectory()) {
                        showFileAndFolder(item.fileAndFolder, Constant.INTERNAL_STORAGE_FILE_FOLDER);
                    }

                }
                return true;
            }
        });

    }

    void openFile(File file, String type) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                getApplicationContext().getApplicationContext().getPackageName() +
                        ".provider", file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (type.equals(Constant.PHOTO_FILE)) {
            intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "image/*");
        }

        if (type.equals(Constant.PDF_FILE)) {
            intent.setDataAndType(photoURI, "application/pdf");
        }

        if (type.equals(Constant.AUDIO_FILE)) {
            intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "audio/*");
        }

        if (type.equals(Constant.VIDEO_FILE)) {
            intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "video/*");
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "openFile: " + e);
        }
    }


    private void scanDirectory(File directory, String type) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {

                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file, type);
                    } else {
                        fileScanBySuffix(file, type);
                    }

                }
            }
        }
    }

    void fileScanBySuffix(File file, String type) {
        if (type.equals(Constant.AUDIO_FILE)) {
            if (file.getName().endsWith(".mp3")) {
                fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.VIDEO_FILE)) {
            if (file.getName().endsWith(".mp4")) {
                fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.PHOTO_FILE)) {
            if (file.getName().endsWith(".jpg")) {
                fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.PDF_FILE)) {
            if (file.getName().endsWith(".pdf")) {
                fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.DOCUMENTS_FILE)) {
            if (file.getName().endsWith("pdf")
                    || file.getName().endsWith("txt")
                    || file.getName().endsWith("xlsx")
                    || file.getName().equals("csv")
                    || file.getName().equals("pptx")) {
                fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.ALL_FILE)) {
            fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
        }

    }


    private void formIntentGetData() {
        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.PHOTO_FILE)) {
            File file = Environment.getExternalStorageDirectory();
            showPhotoFolder(file);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DOWNLOAD_FOLDER)) {
            Log.d(TAG, "formIntentGetData: down");
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.SAFE_BOX_FOLDER)) {
            Toast.makeText(MainActivity.this, Constant.SAFE_BOX_FOLDER, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.MUSIC_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.AUDIO_FILE);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.RECENT_FILE)) {
            Toast.makeText(MainActivity.this, Constant.RECENT_FILE, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DOCUMENTS_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.DOCUMENTS_FILE);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.APP_MANAGER_FOLDER)) {
            Toast.makeText(MainActivity.this, Constant.APP_MANAGER_FOLDER, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.VIDEO_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.VIDEO_FILE);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.AAD_TO_QUICK_ACCESS)) {
            Toast.makeText(MainActivity.this, Constant.AAD_TO_QUICK_ACCESS, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.INTERNAL_STORAGE_PATH)) {
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER);
            backCount = 1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.FOLDER_PATH)) {
            String path = getIntent().getStringExtra(Constant.FOLDER_PATH);
            File file = new File(path);
            showFileAndFolder(file, "all");
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DCIM_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.PICTURES_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.MOVIES_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER);
            backCount = -1;
        }
    }


    private void showFileAndFolder(File mainFile, String requiredFile) {
        Log.d(TAG, "formIntentGetData: audio21");
        fileAndFolderAdapterFastItemAdapter.clear();
        if (!Constant.checkPermission(MainActivity.this)) {
            Log.d(TAG, "showFileAndFolder: d1");
            binding.noFileAvailable.setText("Permission required for display file");
            binding.noFileAvailable.setVisibility(View.VISIBLE);
        }
        List<File> filesAndFolders = Arrays.asList(Objects.requireNonNull(mainFile.listFiles()));
        if (filesAndFolders.size() == 0) {
            Log.d(TAG, "showFileAndFolder: d2");
            binding.noFileAvailable.setVisibility(View.VISIBLE);
            Log.d(TAG, "empty folder");
        } else {
            binding.noFileAvailable.setVisibility(View.GONE);
            fileAndFolderAdapters = new ArrayList<>();

            if (requiredFile.equals(Constant.AUDIO_FILE)) {
                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        Log.d(TAG, "showFileAndFolder: Audio file");
                        scanDirectory(file, Constant.AUDIO_FILE);
                    } else {
                        fileScanBySuffix(file, Constant.AUDIO_FILE);
                    }
                }
            } else if (requiredFile.equals(Constant.DOCUMENTS_FILE)) {
                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        scanDirectory(file, Constant.DOCUMENTS_FILE);
                    } else {
                        fileScanBySuffix(file, Constant.DOCUMENTS_FILE);
                    }
                }
            } else if (requiredFile.equals(Constant.PHOTO_FILE)) {

                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        scanDirectory(file, Constant.PHOTO_FILE);
                        //fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    } else {
                        fileScanBySuffix(file, Constant.PHOTO_FILE);
                    }
                }
            } else if (requiredFile.equals(Constant.VIDEO_FILE)) {

                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        scanDirectory(file, Constant.VIDEO_FILE);
                        //fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    } else {
                        fileScanBySuffix(file, Constant.VIDEO_FILE);
                    }
                }
            } else if (requiredFile.equals(Constant.INTERNAL_STORAGE_FILE_FOLDER)) {
                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        if (Objects.requireNonNull(file.listFiles()).length > 0) {
                            fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                        } else {
                            fileAndFolderAdapters.clear();
                            Log.d(TAG, "showFileAndFolder: 45");
                            binding.noFileAvailable.setVisibility(View.VISIBLE);
                        }
                    }
                }
                for (File file : filesAndFolders) {
                    if (file.isFile()) {
                        binding.noFileAvailable.setVisibility(View.GONE);
                        fileAndFolderAdapters.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    }
                }

            }


            fileAndFolderAdapterFastItemAdapter.add(fileAndFolderAdapters);
            binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            binding.rec.setAdapter(fileAndFolderAdapterFastItemAdapter);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 111) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                formIntentGetData();
            }
        }
    }

    @Override
    public void onBackPressed() {
        binding.noFileAvailable.setVisibility(View.GONE);
        Log.e(TAG, "onBackPressed: " + backCount);
        if (backCount == -1) {
            backCount = 0;
            super.onBackPressed();
        } else if (backCount == 0 || peekPath) {
            if (peekPath) {
                destinationPath = path;
                peekPath = false;
            }
            File parent = new File(destinationPath);
            parent = parent.getParentFile();
            destinationPath = parent.getAbsolutePath();
            fileAndFolderAdapterFastItemAdapter.clear();
            showFileAndFolder(parent, Constant.INTERNAL_STORAGE_FILE_FOLDER);

            if (destinationPath.equals("/storage/emulated/0")) {
                backCount = 1;
            } else {
                backCount = 0;
            }

        } else {
            super.onBackPressed();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage event) {
        if (event.isDelete()) {
            String deletePath = event.getDeleteFilePath();
            Log.d(TAG, "onMessageEvent: " + deletePath);
            File file = new File(deletePath);
            showFileAndFolder(file, "all");
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



    void separatePhotoFolder(File file){
        boolean havefile = false;
        File[] mfiles = file.listFiles();
        for (File file1:mfiles){
            if (!havefile&&file1.getName().endsWith(".jpg")&&file1.isFile()){
                photoGridAdapters.add(new PhotoGridAdapter(MainActivity.this,file));
                havefile = true;
            }else{
                if (!file1.getName().startsWith(".")&&file1.isDirectory()){
                    separatePhotoFolder(file1);
                }

            }
        }

    }


    void showPhotoFolder(File file){
        separatePhotoFolder(file);
        photoGridAdapterFastItemAdapter.add(photoGridAdapters);
        binding.rec.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
        binding.rec.setAdapter(photoGridAdapterFastItemAdapter);
    }

    void showPhotoInFolder(File file){
        photoGridAdapters.clear();
        File[] files = file.listFiles();
        assert files != null;
        for (File file1:files){
            if(file1.getName().endsWith(".jpg")){
                photoGridAdapters.add(new PhotoGridAdapter(MainActivity.this,file1));
            }
        }
        photoGridAdapterFastItemAdapter.add(photoGridAdapters);
        binding.rec.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
        binding.rec.setAdapter(photoGridAdapterFastItemAdapter);
    }

}