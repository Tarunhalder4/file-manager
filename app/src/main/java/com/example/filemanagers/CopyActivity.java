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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    File destinationFile = null;
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
                Task.callInBackground(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {

                        try {
                            if (Constant.MOVE) {
                                multipleFileMove();
                            } else {
                                multipleFileAndFolderCopy();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "onClick: ", e);
                        }

                        return null;
                    }
                }).continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {

                        if (task.isCompleted()) {
                            Log.e(TAG, "then: task is complted");
                        }

                        task.onSuccess(new Continuation<Object, Object>() {
                            @Override
                            public Object then(Task<Object> task) throws Exception {
                                binding.copyToolBarBottom.setVisibility(View.GONE);
                                showFileAndFolder(destinationFile, true);
                                if (getSupportActionBar() != null) {
                                    if (Constant.MOVE) {
                                        getSupportActionBar().setTitle(getResources().getString(R.string.file_move));
                                    } else {
                                        getSupportActionBar().setTitle(getResources().getString(R.string.file_copy));
                                    }
                                }
                                return null;
                            }
                        });

                        if (task.isFaulted()) {
                            Log.e(TAG, "then: ", task.getError());
                        }

                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);


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

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    void multipleFileAndFolderCopy() throws IOException {

        String sourcePath = getIntent().getStringExtra(Constant.PATH);
        ArrayList<Object> listdata = new ArrayList<Object>();
        try {
            JSONArray jsonArray = new JSONArray(sourcePath);
            for (int i = 0; i < jsonArray.length(); i++) {
                listdata.add(jsonArray.get(i));
            }

        } catch (JSONException e) {
            Log.e(TAG, "multipleFileCopy: ", e);
        }

        destinationFile = new File(destinationPath);

        int copedFile = 0;
        for (Object o : listdata) {

            String filePath = o.toString();
            File sourceFile = new File(filePath);

            int totalFile = listdata.size();
            copedFile = copedFile + 1;

            int finalCopedFile = copedFile;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.fileName.setVisibility(View.VISIBLE);
                    binding.fileName.setText(sourceFile.getName());
                    binding.numberOfItem.setVisibility(View.VISIBLE);
                    binding.numberOfItem.setText(finalCopedFile + "/" + totalFile);
                }
            });

            if (sourceFile.isFile()) {
                singleFileCopy(sourceFile.getAbsolutePath(), destinationPath + "/" + sourceFile.getName());
            } else {
                long totalFolderMemory = Constant.getDirectoryMemorySize(new File(sourceFile.getAbsolutePath()));
                singleFolderCopy(sourceFile.getAbsolutePath(), destinationPath + "/" + sourceFile.getName(), totalFolderMemory);
            }

        }


    }


    private void singleFileCopy(String sourcePath, String destinationPath) throws IOException {

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            File file = new File(sourcePath);
            long totalFileSize = file.length();
            fis = new FileInputStream(sourcePath);
            fos = new FileOutputStream(destinationPath);

            int c;
            byte[] buffer = new byte[512];
            while ((c = fis.read(buffer)) != -1) {
                long copyByte = totalFileSize - fis.available();
                long percentByte = ((copyByte * 100) / totalFileSize);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.copyProgressbar.setVisibility(View.VISIBLE);
                        binding.pastImage.setVisibility(View.GONE);
                        binding.past.setVisibility(View.GONE);
                        binding.cancelImage.setVisibility(View.GONE);
                        binding.cancel.setVisibility(View.GONE);
                        binding.copyProgressbar.setProgress((int) percentByte);
                    }
                });

                fos.write(buffer, 0, c);
            }

            Log.e(TAG, " copied the file successfully ");

        } catch (Exception e) {
            Log.e(TAG, "singleFileCopy: ", e);
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }

        }

    }

    public void singleFolderCopy(String source, String destination, long totalFolderMemory) {

        File sourceFolder = new File(source);
        File destinationFolder = new File(destination);

        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists()) {
                boolean make = destinationFolder.mkdirs();
                if (make) {
                    Log.e(TAG, "make destination folder: ");
                }
            }

            String[] files = sourceFolder.list();

            assert files != null;
            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);
                singleFolderCopy(srcFile.getAbsolutePath(), destFile.getAbsolutePath(), totalFolderMemory);
            }
        } else {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);

                byte[] buffer = new byte[512];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    long copyByte = totalFolderMemory - in.available();
                    long percentByte = ((copyByte * 100) / totalFolderMemory);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.copyProgressbar.setVisibility(View.VISIBLE);
                            binding.pastImage.setVisibility(View.GONE);
                            binding.past.setVisibility(View.GONE);
                            binding.cancelImage.setVisibility(View.GONE);
                            binding.cancel.setVisibility(View.GONE);
                            binding.copyProgressbar.setProgress((int) percentByte);
                        }
                    });
                    out.write(buffer, 0, length);
                }

                Log.e(TAG, " copied the folder successfully ");
            } catch (Exception e) {
                try {
                    assert in != null;
                    in.close();
                } catch (IOException e1) {
                    Log.e(TAG, "singleFolderCopy: ", e1);
                }

                try {
                    assert out != null;
                    out.close();
                } catch (IOException e1) {
                    Log.e(TAG, "singleFolderCopy: ", e1);
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    void multipleFileMove() throws IOException {
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

        destinationFile = new File(destinationPath);

        int copedFile = 0;
        for (Object o : listdata) {

            String filePath = o.toString();
            File sourceFile = new File(filePath);

            int totalFile = listdata.size();
            copedFile = copedFile + 1;

            int finalCopedFile = copedFile;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.fileName.setVisibility(View.VISIBLE);
                    binding.fileName.setText(sourceFile.getName());
                    binding.numberOfItem.setVisibility(View.VISIBLE);
                    binding.numberOfItem.setText(finalCopedFile + "/" + totalFile);
                }
            });
            Log.e(TAG, "call: file copy");

            singleFileMove(sourceFile.getAbsolutePath(), destinationPath + "/" + sourceFile.getName());

        }

    }

    private void singleFileMove(String sourcePath, String destinationPath) throws IOException {

        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            File file = new File(sourcePath);
            long totalFileSize = file.length();
            fis = new FileInputStream(sourcePath);
            fos = new FileOutputStream(destinationPath);

            int c;
            byte[] buffer = new byte[512];

            while ((c = fis.read(buffer)) != -1) {
                long copyByte = totalFileSize - fis.available();
                long percentByte = ((copyByte * 100) / totalFileSize);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.copyProgressbar.setVisibility(View.VISIBLE);
                        binding.pastImage.setVisibility(View.GONE);
                        binding.past.setVisibility(View.GONE);
                        binding.cancelImage.setVisibility(View.GONE);
                        binding.cancel.setVisibility(View.GONE);
                        binding.copyProgressbar.setProgress((int) percentByte);
                    }
                });

                fos.write(buffer, 0, c);
                // fos.write(c);
            }

            new File(sourcePath).delete();

            Log.e(TAG, " copied the file successfully ");

        } catch (Exception e) {
            Log.e(TAG, "singleFileCopy: ", e);
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

    public static void deleteFolder(File file){
        for (File subFile : file.listFiles()) {
            if(subFile.isDirectory()) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }
}


