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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class CopyActivity extends AppCompatActivity {
    ActivityCopyBinding binding;
    private FastItemAdapter<FileAndFolderAdapter> fastItemAdapter;
    ArrayList<File> newFiles;
    String path;
    int backCount = 0;
    String destinationPath = null;
    String TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCopyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.file_copying));

        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);
        newFiles = new ArrayList<>();

        File file = Environment.getExternalStorageDirectory();
        showFileAndFolder(file, true);

        fastItemAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {

                if (item.fileAndFolder.isDirectory()) {
                    path = item.fileAndFolder.getPath();
                    File file = new File(path);
                    destinationPath = file.getAbsolutePath();
                    showFileAndFolder(file, true);
                }
                return true;
            }
        });

        binding.pastImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                multipleFileCopy();
            }
        });

        binding.cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void showFileAndFolder(File mainFile, boolean fileAndFolder) {
        fastItemAdapter.clear();
        List<File> filesAndFolders = Arrays.asList(Objects.requireNonNull(mainFile.listFiles()));
        if (filesAndFolders.size() == 0) {
            binding.noFileAvailable.setVisibility(View.VISIBLE);
        } else {
            binding.noFileAvailable.setVisibility(View.GONE);
            List<FileAndFolderAdapter> fileAndFolderAdapters = new ArrayList<>();

            for (File file : filesAndFolders) {
                if (file.isDirectory()) {
                    fileAndFolderAdapters.add(new FileAndFolderAdapter(file, CopyActivity.this, null));
                }
            }

            if (fileAndFolder) {
                for (File file : filesAndFolders) {
                    if (file.isFile()) {
                        fileAndFolderAdapters.add(new FileAndFolderAdapter(file, CopyActivity.this, null));
                    }
                }
            }
            fastItemAdapter.add(fileAndFolderAdapters);
            binding.copyRecActivity.setLayoutManager(new LinearLayoutManager(CopyActivity.this));
            binding.copyRecActivity.setAdapter(fastItemAdapter);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void multipleFileCopy() {
        String sourcePath = getIntent().getStringExtra(Constant.PATH);
        ArrayList<Object> listdata = new ArrayList<Object>();
        try {
            JSONArray jsonArray = new JSONArray(sourcePath);
            for (int i = 0; i < jsonArray.length(); i++) {
                listdata.add(jsonArray.get(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        File destinationFile = new File(destinationPath);
        for (Object o : listdata) {
            String filePath = o.toString();
            File sourceFile = new File(filePath);
        Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Log.e(TAG, "call: file copy" );
                   // Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(destinationPath + "/" + sourceFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                    singleFileCopy(sourceFile.getAbsolutePath(), destinationPath + "/" + sourceFile.getName());
                   // Constant.copyFile(sourcePath,sourceFile.getName(),destinationPath);
                    return null;
                }
            }).continueWith(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {

                    if(task.isCompleted()){
                        getSupportActionBar().setTitle(getResources().getString(R.string.file_copy));
                        Log.e(TAG, "then: file copy" );

                        showFileAndFolder(destinationFile, true);
                        binding.copyToolBarBottom.setVisibility(View.GONE);

                    }

                    if(task.isFaulted()){
                        Log.e(TAG, "then: faulted" );
                    }

                    if (task.isCancelled()){
                        Log.e(TAG, "then: task cancel" );
                    }


                    return null;
                }
            },Task.UI_THREAD_EXECUTOR);

        }


    }


    private void singleFileCopy(String sourcePath, String destinationPath) throws IOException {

        FileInputStream fis = null;
        FileOutputStream fos = null;

        Log.e(TAG, "singleFileCopy: 12" );

        try {
            Log.e(TAG, "singleFileCopy: 123" );
            File file = new File(sourcePath);
            int totalFileSize = (int) file.length();
            fis = new FileInputStream(sourcePath);
            fos = new FileOutputStream(destinationPath);

            int c;
            byte[] buffer = new byte[1024];

            while ((c = fis.read(buffer)) != -1) {
                Log.e(TAG, "singleFileCoping.........: " );
                int copyByte = totalFileSize-fis.available();
                Log.e(TAG, "percentByte : "+ (copyByte*100)/totalFileSize);
                Log.e(TAG, "totalFileSize : "+totalFileSize );
                int percentByte = ((copyByte*100)/totalFileSize);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.copyProgressbar.setVisibility(View.VISIBLE);
                        binding.pastImage.setVisibility(View.GONE);
                        binding.past.setVisibility(View.GONE);
                        binding.cancelImage.setVisibility(View.GONE);
                        binding.cancel.setVisibility(View.GONE);
                        binding.copyProgressbar.setProgress(percentByte);
                    }
                });

                fos.write(buffer, 0,c);
               // fos.write(c);
            }

            Log.e(TAG, " copied the file successfully " );

        }catch (Exception e){
            Log.e(TAG, "singleFileCopy: ",e);
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (backCount == 0) {
            destinationPath = path;
        }

        File parent = new File(destinationPath);
        parent = parent.getParentFile();
        destinationPath = parent.getAbsolutePath();
        fastItemAdapter.clear();
        showFileAndFolder(parent, true);
        backCount = 1;

        if (destinationPath.equals("/storage/emulated/0")) {
            super.onBackPressed();
        }
    }

}


