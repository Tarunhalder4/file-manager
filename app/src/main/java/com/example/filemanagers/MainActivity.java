package com.example.filemanagers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;

import java.io.IOException;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filemanagers.adapter.HeaderItem;
import com.example.filemanagers.adapter.PathAdapter;
import com.example.filemanagers.adapter.PhotoGridAdapter;
import com.example.filemanagers.databinding.ActivityMainBinding;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.ISelectionListener;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter.listeners.OnLongClickListener;
import com.mikepenz.fastadapter_extensions.ActionModeHelper;
import com.mikepenz.fastadapter_extensions.UndoHelper;
import com.mikepenz.materialize.util.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class MainActivity extends AppCompatActivity {

    String path;
    int backCount = 0;
    String destinationPath = null;
    boolean peekPath = false;
    boolean backPress = false;

    private String TAG = "tag";
    private ActivityMainBinding binding;
    private SharePref sharePref;

    private FastAdapter<AbstractItem> fileAndFolderFastAdapter;
    private ItemAdapter<AbstractItem> fileAndFolderItemAdapter;
    private List<AbstractItem> fileAndFolderAdapterList;

    private FastItemAdapter<PhotoGridAdapter> photoGridAdapterFastItemAdapter;
    private FastItemAdapter<PathAdapter> pathAdapterFastItemAdapter;

    private List<PhotoGridAdapter> photoGridAdapterList;
    private ArrayDeque<PathAdapter> pathAdapterArrayDeque;
    private List<PathAdapter> pathAdapterList;

    private ActionModeHelper<AbstractItem> mActionModeHelper;
    private UndoHelper mUndoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_files);
        sharePref = SharePref.getInstance(MainActivity.this);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.folder_background_dark));

        fileAndFolderItemAdapter = new ItemAdapter<>();
        fileAndFolderFastAdapter = FastAdapter.with(fileAndFolderItemAdapter);

        photoGridAdapterFastItemAdapter = new FastItemAdapter<>();
        pathAdapterFastItemAdapter = new FastItemAdapter<>();

        fileAndFolderFastAdapter.setHasStableIds(true);
        fileAndFolderFastAdapter.withSelectable(true);
        fileAndFolderFastAdapter.withMultiSelect(true);
        fileAndFolderFastAdapter.withSelectOnLongClick(true);

        photoGridAdapterFastItemAdapter.withSelectable(true);
        pathAdapterFastItemAdapter.withSelectable(true);

        mActionModeHelper = new ActionModeHelper<>(fileAndFolderFastAdapter, R.menu.multiselect_menu, new ActionBarCallBack());

        photoGridAdapterList = new ArrayList<>();
        pathAdapterArrayDeque = new ArrayDeque<>();
        pathAdapterList = new ArrayList<>();

        if (Constant.checkPermission(MainActivity.this)) {
            binding.progressBar.setVisibility(View.VISIBLE);
            formIntentGetData();
        } else {
            Constant.requestPermission(MainActivity.this);
        }

        pathAdapterFastItemAdapter.withOnClickListener(new OnClickListener<PathAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<PathAdapter> adapter, PathAdapter item, int position) {
                if (item.file.isDirectory()) {
                    path = item.file.getAbsolutePath();
                    pathAdapterArrayDeque.clear();
                    pathAdapterList.clear();
                    pathAdapterFastItemAdapter.clear();

                    String[] arrOfStr = item.file.getPath().split("/");

                    File file = item.file.getParentFile();
                    for (int i = 4; i <= arrOfStr.length - 1; i++) {
                        assert file != null;
                        pathAdapterArrayDeque.offerFirst(new PathAdapter(file));
                        file = file.getParentFile();

                    }

                    pathAdapterList.addAll(pathAdapterArrayDeque);
                    pathAdapterFastItemAdapter.add(pathAdapterList);
                    binding.pathRec.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    binding.pathRec.setAdapter(pathAdapterFastItemAdapter);

                    showFileAndFolder(item.file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
                }
                return false;
            }
        });

        photoGridAdapterFastItemAdapter.withOnClickListener(new OnClickListener<PhotoGridAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<PhotoGridAdapter> adapter, PhotoGridAdapter item, int position) {

                if (item.file.isDirectory()) {
                    photoGridAdapterFastItemAdapter.clear();
                    showPhotoInFolder(item.file);
                } else {
                    if (item.file.isFile() && item.file.getName().endsWith("jpg") || item.file.getName().endsWith(".png")) {
                        openFile(item.file, Constant.PHOTO_FILE);
                    }
                }

                return true;
            }
        });


        fileAndFolderFastAdapter.withSelectionListener(new ISelectionListener<AbstractItem>() {
            @Override
            public void onSelectionChanged(AbstractItem item, boolean selected) {
                Log.i("FastAdapter", "SelectedCount: " + fileAndFolderFastAdapter.getSelections().size() + " ItemsCount: " + fileAndFolderFastAdapter.getSelectedItems().size());
                // Set<FileAndFolderAdapter> fileAndFolderAdapters = fileAndFolderFastAdapter.getSelectedItems();
            }
        });

        fileAndFolderFastAdapter.withOnPreClickListener(new OnClickListener<AbstractItem>() {
            @Override
            public boolean onClick(View v, IAdapter<AbstractItem> adapter, @NonNull AbstractItem item, int position) {
                //we handle the default onClick behavior for the actionMode. This will return null if it didn't do anything and you can handle a normal onClick
                Boolean res = mActionModeHelper.onClick(item);
                return res != null ? res : false;
            }
        });

        fileAndFolderFastAdapter.withOnClickListener(new OnClickListener<AbstractItem>() {
            @Override
            public boolean onClick(View v, IAdapter<AbstractItem> adapter, AbstractItem item, int position) {
                if (item instanceof FileAndFolderAdapter) {
                    if (!Constant.LOG_CLICK_ACTIVATED) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        if (((FileAndFolderAdapter) item).fileAndFolder.isDirectory()) {
                            binding.noFileAvailable.setVisibility(View.GONE);
                            path = ((FileAndFolderAdapter) item).fileAndFolder.getPath();
                            peekPath = true;
                            showFileAndFolder(((FileAndFolderAdapter) item).fileAndFolder, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
                        } else {
                            if (((FileAndFolderAdapter) item).fileAndFolder.getName().endsWith(".pdf")) {
                                openFile(((FileAndFolderAdapter) item).fileAndFolder, Constant.PDF_FILE);
                            }

                            if (((FileAndFolderAdapter) item).fileAndFolder.getName().endsWith(".jpg") || ((FileAndFolderAdapter) item).fileAndFolder.getName().endsWith(".png")) {
                                openFile(((FileAndFolderAdapter) item).fileAndFolder, Constant.PHOTO_FILE);
                            }

                            if (((FileAndFolderAdapter) item).fileAndFolder.getName().endsWith(".mp3")) {
                                openFile(((FileAndFolderAdapter) item).fileAndFolder, Constant.AUDIO_FILE);
                            }

                            if (((FileAndFolderAdapter) item).fileAndFolder.getName().endsWith(".mp4")) {
                                openFile(((FileAndFolderAdapter) item).fileAndFolder, Constant.VIDEO_FILE);
                            }

                        }
                    } else {
                        if (fileAndFolderFastAdapter.getSelectedItems().size() == 0) {
                            Constant.LOG_CLICK_ACTIVATED = false;
                        }
                        Toast.makeText(v.getContext(), "SelectedCount: " + fileAndFolderFastAdapter.getSelections().size() + " ItemsCount: " + fileAndFolderFastAdapter.getSelectedItems().size(), Toast.LENGTH_SHORT).show();
                        Set<AbstractItem> fileAndFolderAdapters = fileAndFolderFastAdapter.getSelectedItems();
                    }
                }

                return false;
            }
        });

        mUndoHelper = new UndoHelper<>(fileAndFolderFastAdapter, new UndoHelper.UndoListener<AbstractItem>() {
            @Override
            public void commitRemove(Set<Integer> positions, ArrayList<FastAdapter.RelativeInfo<AbstractItem>> removed) {
                Log.e("UndoHelper", "Positions: " + positions.toString() + " Removed: " + removed.size());
            }
        });

        fileAndFolderFastAdapter.withOnPreLongClickListener(new OnLongClickListener<AbstractItem>() {
            @Override
            public boolean onLongClick(View v, IAdapter<AbstractItem> adapter, AbstractItem item, int position) {
                ActionMode actionMode = mActionModeHelper.onLongClick(MainActivity.this, position);
                getSupportActionBar().hide();
                Constant.LOG_CLICK_ACTIVATED = true;
                if (actionMode != null) {
                    v.findViewById(R.id.each_row_layout).setBackgroundColor(UIUtils.getThemeColorFromAttrOrRes(MainActivity.this, androidx.appcompat.R.attr.color,
                            R.color.red));
                }

                //if we have no actionMode we do not consume the event
                return actionMode != null;
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = fileAndFolderFastAdapter.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
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
        binding.progressBar.setVisibility(View.GONE);
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
                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.VIDEO_FILE)) {
            if (file.getName().endsWith(".mp4")) {
                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.PHOTO_FILE)) {
            if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.PDF_FILE)) {
            if (file.getName().endsWith(".pdf")) {
                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.DOCUMENTS_FILE)) {
            if (file.getName().endsWith("pdf")
                    || file.getName().endsWith("xlsx")
                    || file.getName().equals("csv")
                    || file.getName().equals("pptx")) {
                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
            }
        } else if (type.equals(Constant.ALL_FILE)) {
            fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
        }

    }


    private void formIntentGetData() {
        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.PHOTO_FILE)) {
            File file = Environment.getExternalStorageDirectory();
            showPath(file);
            showPhotoFolder(file);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.DOWNLOAD_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.SAFE_BOX_FOLDER)) {
            Toast.makeText(MainActivity.this, Constant.SAFE_BOX_FOLDER, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.MUSIC_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.AUDIO_FILE, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.RECENT_FILE)) {
            Toast.makeText(MainActivity.this, Constant.RECENT_FILE, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.DOCUMENTS_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.DOCUMENTS_FILE, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.APP_MANAGER_FOLDER)) {
            Toast.makeText(MainActivity.this, Constant.APP_MANAGER_FOLDER, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.VIDEO_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.VIDEO_FILE, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.AAD_TO_QUICK_ACCESS)) {
            Toast.makeText(MainActivity.this, Constant.AAD_TO_QUICK_ACCESS, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.INTERNAL_STORAGE_PATH)) {
            path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = 1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.FOLDER_PATH)) {
            String path = getIntent().getStringExtra(Constant.FOLDER_PATH);
            File file = new File(path);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.DCIM_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.PICTURES_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH)!=null && getIntent().getStringExtra(Constant.PATH).equals(Constant.MOVIES_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
            backCount = -1;
        }
    }


    private void showFileAndFolder(File mainFile, String requiredFile, boolean hide, String compareType) {

        fileAndFolderItemAdapter.clear();
        showPath(mainFile);
        if (!Constant.checkPermission(MainActivity.this)) {

            binding.noFileAvailable.setText("Permission required for display file");
            binding.noFileAvailable.setVisibility(View.VISIBLE);
            binding.pathRec.setVisibility(View.GONE);

        }

        List<File> filesAndFolders = null;
        if (mainFile.listFiles() != null) {
            filesAndFolders = Arrays.asList(Objects.requireNonNull(mainFile.listFiles()));
        }

        if (filesAndFolders != null && filesAndFolders.size() > 0) {

            binding.noFileAvailable.setVisibility(View.GONE);


            Log.d(TAG, "call: " + Thread.currentThread().getName());
            fileAndFolderAdapterList = new ArrayList<>();

            if (requiredFile.equals(Constant.AUDIO_FILE)) {
                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        scanDirectory(file, Constant.AUDIO_FILE);
                    } else {
                        fileScanBySuffix(file, Constant.AUDIO_FILE);
                    }
                }

                fileAndFolderItemAdapter.add(fileAndFolderAdapterList);
                binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                binding.rec.setAdapter(fileAndFolderFastAdapter);

            } else if (requiredFile.equals(Constant.DOCUMENTS_FILE)) {
                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        scanDirectory(file, Constant.DOCUMENTS_FILE);
                    } else {
                        fileScanBySuffix(file, Constant.DOCUMENTS_FILE);
                    }
                }

                fileAndFolderItemAdapter.add(fileAndFolderAdapterList);
                binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                binding.rec.setAdapter(fileAndFolderFastAdapter);

            } else if (requiredFile.equals(Constant.PHOTO_FILE)) {

                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        scanDirectory(file, Constant.PHOTO_FILE);
                    } else {
                        fileScanBySuffix(file, Constant.PHOTO_FILE);
                    }
                }

                fileAndFolderItemAdapter.add(fileAndFolderAdapterList);
                binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                binding.rec.setAdapter(fileAndFolderFastAdapter);

            } else if (requiredFile.equals(Constant.VIDEO_FILE)) {

                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        scanDirectory(file, Constant.VIDEO_FILE);
                    } else {
                        fileScanBySuffix(file, Constant.VIDEO_FILE);
                    }
                }

                fileAndFolderItemAdapter.add(fileAndFolderAdapterList);
                binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                binding.rec.setAdapter(fileAndFolderFastAdapter);

            } else if (requiredFile.equals(Constant.INTERNAL_STORAGE_FILE_FOLDER)) {

                Task.callInBackground(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.progressBar.setVisibility(View.VISIBLE);
                            }
                        });
                        File[] files = Objects.requireNonNull(mainFile.listFiles());
                        List<File> fileList = new ArrayList<>(Arrays.asList(files));

                        compare(fileList, compareType);

                        List<File> files1 = new ArrayList<>();
                        for (File file : fileList) {
                            if (file.isDirectory()) {
                                files1.add(file);
                            }
                        }

                        for (File file : fileList) {
                            if (file.isFile()) {
                                files1.add(file);
                            }
                        }

                        for(File file : files1){
                            if(file.isDirectory()){
                                if(file.isHidden() && !sharePref.getShowHiddenFileAndFolder()){

                                }else {
                                fileAndFolderAdapterList.add(new HeaderItem("List of Folder"));
                                break;
                            }
                            }
                        }

                        for (File file : files1) {
                            if (file.isDirectory() && !file.getName().startsWith(".") && hide) {
                                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                            } else if (file.isDirectory() && !hide) {
                                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                            }

                        }

                        for(File file : files1){
                            if(file.isFile()){
                                if(file.isHidden() && !sharePref.getShowHiddenFileAndFolder()){

                                }else {
                                fileAndFolderAdapterList.add(new HeaderItem("List of File"));
                                break;
                                }
                            }
                        }

                        for (File file : files1) {
                            if (file.isFile() && !file.getName().startsWith(".") && hide) {
                                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                            } else if (file.isFile() && !hide) {
                                fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                            }

                        }

                        return null;
                    }
                }).continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {

                        if (task.isCompleted()) {
                            fileAndFolderItemAdapter.add(fileAndFolderAdapterList);
                            binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            binding.rec.setAdapter(fileAndFolderFastAdapter);
                            binding.progressBar.setVisibility(View.GONE);
                        }

                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            }

        } else {
            binding.noFileAvailable.setVisibility(View.VISIBLE);
        }

        binding.progressBar.setVisibility(View.GONE);

    }

    private void compare(List<File> files, String compareType) {
        if (sharePref.getSortAscending()) {
            if (compareType.equals(Constant.NAME_ASCENDING_ORDER)) {
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                    }
                });
            } else if (compareType.equals(Constant.DATE_ASCENDING_ORDER)) {
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return (int) (o1.lastModified() / 1000 - o2.lastModified() / 1000);
                    }
                });
            } else if (compareType.equals(Constant.SIZE_ASCENDING_ORDER)) {
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {

                        if (o1.isFile() && o2.isFile()) {
                            return (int) (o1.length() / 1000 - o2.length() / 1000);
                        } else {
                            return (int) (Constant.getDirectoryMemorySize(o1) / 1000 - Constant.getDirectoryMemorySize(o2) / 1000);
                        }

                    }
                });
            }
        } else {
            if (compareType.equals(Constant.NAME_DESCENDING_ORDER)) {
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o2.getName().toLowerCase().compareTo(o1.getName().toLowerCase());
                    }
                });
            } else if (compareType.equals(Constant.DATE_DESCENDING_ORDER)) {
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return (int) (o2.lastModified() / 1000 - o1.lastModified() / 1000);
                    }
                });
            } else if (compareType.equals(Constant.SIZE_DESCENDING_ORDER)) {
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.isFile() && o2.isFile()) {
                            return (int) (o2.length() / 1000 - o1.length() / 1000);
                        } else {
                            return (int) (Constant.getDirectoryMemorySize(o2) / 1000 - Constant.getDirectoryMemorySize(o1) / 1000);
                        }
                    }
                });
            }
        }

    }

    void showPath(File file) {
        if (backPress) {
            pathAdapterFastItemAdapter.clear();
            pathAdapterList.clear();
            pathAdapterArrayDeque.pollLast();
            backPress = false;
        } else if (Constant.SAME_PATH) {
            pathAdapterFastItemAdapter.clear();
            pathAdapterList.clear();
            Constant.SAME_PATH = false;
        } else {
            pathAdapterFastItemAdapter.clear();
            pathAdapterList.clear();
            pathAdapterArrayDeque.offerLast(new PathAdapter(file));
        }

        pathAdapterList.addAll(pathAdapterArrayDeque);

        pathAdapterFastItemAdapter.add(pathAdapterList);
        binding.pathRec.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
        binding.pathRec.setAdapter(pathAdapterFastItemAdapter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constant.REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                formIntentGetData();
            }
        }
    }

    @Override
    public void onBackPressed() {
        backPress = true;

        binding.noFileAvailable.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

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
            fileAndFolderItemAdapter.clear();

            showFileAndFolder(parent, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());

            if (destinationPath.equals("/storage/emulated")) {
                binding.noFileAvailable.setVisibility(View.GONE);
                backCount = 1;
                super.onBackPressed();
            } else {
                backCount = 0;
            }

        } else {
            super.onBackPressed();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage event) {
        if (event.isFileDelete()) {
            String Path = event.getFilePath();
            File file = new File(Path);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER,!sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
        } else if (event.isFileRename()) {
            String Path = event.getFilePath();
            File file = new File(Path);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER,!sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
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

    void separatePhotoFolder(File file) {
        boolean havefile = false;
        File[] mfiles = file.listFiles();
        if (mfiles != null && mfiles.length > 0) {
            for (File file1 : mfiles) {
                if (!havefile && file1.isFile()) {
                    if (file1.getName().endsWith(".jpg") || file1.getName().endsWith(".png")) {
                        photoGridAdapterList.add(new PhotoGridAdapter(MainActivity.this, file));
                        havefile = true;
                    }
                } else {
                    if (!file1.getName().startsWith(".") && file1.isDirectory()) {
                        separatePhotoFolder(file1);
                    }

                }
            }
        }

    }

    void showPhotoFolder(File file) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Log.e(TAG, "call:123 ");
                separatePhotoFolder(file);
                photoGridAdapterFastItemAdapter.add(photoGridAdapterList);
                binding.rec.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                binding.rec.setAdapter(photoGridAdapterFastItemAdapter);
                binding.pathRec.setVisibility(View.GONE);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getResources().getString(R.string.my_photos));
                }

                return null;
            }
        });

        binding.progressBar.setVisibility(View.GONE);

    }

    void showPhotoInFolder(File file) {
        photoGridAdapterList.clear();
        File[] files = file.listFiles();
        assert files != null;
        for (File file1 : files) {
            if (file1.getName().endsWith(".jpg") || file1.getName().endsWith(".png")) {
                photoGridAdapterList.add(new PhotoGridAdapter(MainActivity.this, file1));
            }
        }
        photoGridAdapterFastItemAdapter.add(photoGridAdapterList);
        binding.rec.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
        binding.rec.setAdapter(photoGridAdapterFastItemAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_file_menu, menu);
        //inflater.inflate(R.menu.multiselect_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        MenuItem showFileSize, showFolderSize, showFullNameOfFile, showHiddenFileAndFolder;

        showFileSize = menu.findItem(R.id.show_file_size);
        showFolderSize = menu.findItem(R.id.show_folder_size);
        // showFullNameOfFile = menu.findItem(R.id.show_full_name_of_files);
        showHiddenFileAndFolder = menu.findItem(R.id.show_hidden_folder_files);

        showFileSize.setChecked(sharePref.getShowFileSize());
        showFolderSize.setChecked(sharePref.getShowFolderSize());
        // showFullNameOfFile.setChecked(sharePref.getShowFullNameOfFile());
        showHiddenFileAndFolder.setChecked(sharePref.getShowHiddenFileAndFolder());

        return super.onMenuOpened(featureId, menu);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sort:
                sortingDialogBox();
                break;
            case R.id.create_file:
                try {
                    createFile(path);
                } catch (IOException e) {
                    Log.e(TAG, "onOptionsItemSelected: ", e);
                }
                break;
            case R.id.create_folder:
                createFolder(path);
                break;
            case R.id.view_option:
                break;
            case R.id.close:
                finish();
                break;
            case R.id.show_file_size:
                if (item.isCheckable()) {
                    if (!sharePref.getShowFileSize()) {
                        Constant.SAME_PATH = true;
                        sharePref.setShowFileSize(true);
                        item.setChecked(true);
                        File file = new File(path);
                        showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
                    } else {
                        Constant.SAME_PATH = true;
                        sharePref.setShowFileSize(false);
                        item.setChecked(false);
                        File file = new File(path);
                        showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
                    }

                }
                break;
            case R.id.show_folder_size:
                if (item.isCheckable()) {
                    if (!sharePref.getShowFolderSize()) {
                        Constant.SAME_PATH = true;
                        sharePref.setShowFolderSize(true);
                        item.setChecked(true);
                        File file = new File(path);
                        showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
                    } else {
                        Constant.SAME_PATH = true;
                        sharePref.setShowFolderSize(false);
                        item.setChecked(false);
                        File file = new File(path);
                        showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
                    }

                }
                break;
//            case R.id.show_full_name_of_files:
//                break;
            case R.id.show_hidden_folder_files:
                if (item.isCheckable()) {
                    if (!sharePref.getShowHiddenFileAndFolder()) {
                        Constant.SAME_PATH = true;
                        sharePref.setShowHiddenFileAndFolder(true);
                        item.setChecked(true);
                        File file = new File(path);
                        showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, false, sharePref.getCompareType());
                    } else {
                        Constant.SAME_PATH = true;
                        sharePref.setShowHiddenFileAndFolder(false);
                        item.setChecked(false);
                        File file = new File(path);
                        showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, true, sharePref.getCompareType());
                    }

                }
                break;
            case R.id.multiselect_item_delete:
                Toast.makeText(MainActivity.this, "file delete", Toast.LENGTH_SHORT).show();
//                if(fileAndFolderFastAdapter.getSelectedItems().size()>0){
//                    multiselectDelete();
//                }
                break;
            case R.id.multiselect_item_copy:
                Toast.makeText(MainActivity.this, "file copy", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void createFolder(String path) {

        EditText fileName;
        TextView save, cancel, title;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View customLayout = getLayoutInflater().inflate(R.layout.rename_dialog_box, null);
        builder.setView(customLayout);

        fileName = customLayout.findViewById(R.id.rename_edit_box);
        save = customLayout.findViewById(R.id.rename_save);
        cancel = customLayout.findViewById(R.id.rename_cancel);
        title = customLayout.findViewById(R.id.rename_title);

        fileName.setHint("");
        title.setText(getResources().getString(R.string.make_directory_title));

        AlertDialog dialog = builder.create();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fileName.getText().toString().trim().equals("")) {
                    String folderName = fileName.getText().toString().trim();
                    Log.e(TAG, "onClick: " + path);
                    File file = new File(path + "/" + folderName);

                    if (file.mkdir()) {
                        String filePath = file.getParent();
                        if (filePath != null) {
                            File currentFile = new File(filePath);
                            Constant.SAME_PATH = true;
                            showFileAndFolder(currentFile, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
                            Toast.makeText(MainActivity.this, "Folder created", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Folder not created", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                } else {
                    Toast.makeText(MainActivity.this, "Please entry folder name", Toast.LENGTH_SHORT).show();
                }

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

    private void createFile(String path) throws IOException {

        EditText fileName;
        TextView save, cancel, title;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View customLayout = getLayoutInflater().inflate(R.layout.rename_dialog_box, null);
        builder.setView(customLayout);

        fileName = customLayout.findViewById(R.id.rename_edit_box);
        save = customLayout.findViewById(R.id.rename_save);
        cancel = customLayout.findViewById(R.id.rename_cancel);
        title = customLayout.findViewById(R.id.rename_title);

        fileName.setHint("");
        title.setText(getResources().getString(R.string.make_file_title));

        AlertDialog dialog = builder.create();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fileName.getText().toString().trim().equals("")) {
                    String newFileName = fileName.getText().toString().trim();
                    File file = new File(path + "/" + newFileName);

                    try {
                        if (file.createNewFile()) {
                            String filePath = file.getParent();
                            if (filePath != null) {
                                File currentFile = new File(filePath);
                                Constant.SAME_PATH = true;
                                showFileAndFolder(currentFile, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), sharePref.getCompareType());
                                Toast.makeText(MainActivity.this, "File created", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "File not created", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "onClick: ", e);
                    }
                    dialog.dismiss();

                } else {
                    Toast.makeText(MainActivity.this, "Please entry file name", Toast.LENGTH_SHORT).show();
                }

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


//    private void multiselectDelete() {
//
//        for (FileAndFolderAdapter fileAndFolderAdapter : fileAndFolderFastAdapter.getSelectedItems()) {
//            if (fileAndFolderAdapter.fileAndFolder.delete()) {
//                Toast.makeText(MainActivity.this, fileAndFolderAdapter.fileAndFolder.getName() + " file is delete", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(MainActivity.this, fileAndFolderAdapter.fileAndFolder.getName() + " file is delete", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    void sortingDialogBox() {
        TextView ascendingOrder;
        RadioGroup radioGroup;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.sort_dialog_box, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        ascendingOrder = view.findViewById(R.id.sort_ascending);
        radioGroup = view.findViewById(R.id.radio_group);

        if (sharePref.getSortAscending()) {
            ascendingOrder.setText(getResources().getString(R.string.ascending_order));
        } else {
            ascendingOrder.setText(getResources().getString(R.string.descending_order));
        }

        ascendingOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharePref.getSortAscending()) {
                    Constant.ASCENDING_ORDER = false;
                    sharePref.setSortAscending(false);
                    ascendingOrder.setText(getResources().getString(R.string.descending_order));
                    setAscendingAndDescendingOrder(sharePref.getSortId());
                    Toast.makeText(MainActivity.this, "ascending order", Toast.LENGTH_SHORT).show();
                } else {
                    Constant.ASCENDING_ORDER = true;
                    sharePref.setSortAscending(true);
                    ascendingOrder.setText(getResources().getString(R.string.ascending_order));
                    setAscendingAndDescendingOrder(sharePref.getSortId());
                    Toast.makeText(MainActivity.this, "descending order", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        int sortingId = sharePref.getSortId();
        if (sortingId != 0) {
            radioGroup.check(sortingId);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Constant.checkedId = checkedId;
                setAscendingAndDescendingOrder(checkedId);
                dialog.dismiss();
            }
        });

    }

    @SuppressLint("NonConstantResourceId")
    private void setAscendingAndDescendingOrder(int checkedId) {
        sharePref.setSortId(checkedId);
        File file = new File(path);
        if (sharePref.getSortAscending()) {
            switch (checkedId) {
                case R.id.sort_name:
                    sharePref.setCompareType(Constant.NAME_ASCENDING_ORDER);
                    Constant.SAME_PATH = true;
                    showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), Constant.NAME_ASCENDING_ORDER);
                    break;
                case R.id.sort_last_modified_date:
                    sharePref.setCompareType(Constant.DATE_ASCENDING_ORDER);
                    Constant.SAME_PATH = true;
                    showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), Constant.DATE_ASCENDING_ORDER);
                    break;
                case R.id.sort_size:
                    sharePref.setCompareType(Constant.SIZE_ASCENDING_ORDER);
                    Constant.SAME_PATH = true;
                    showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), Constant.SIZE_ASCENDING_ORDER);
            }
        } else {
            switch (checkedId) {
                case R.id.sort_name:
                    sharePref.setCompareType(Constant.NAME_DESCENDING_ORDER);
                    Constant.SAME_PATH = true;
                    showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), Constant.NAME_DESCENDING_ORDER);
                    break;
                case R.id.sort_last_modified_date:
                    sharePref.setCompareType(Constant.DATE_DESCENDING_ORDER);
                    Constant.SAME_PATH = true;
                    showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), Constant.DATE_DESCENDING_ORDER);
                    break;
                case R.id.sort_size:
                    sharePref.setCompareType(Constant.SIZE_DESCENDING_ORDER);
                    Constant.SAME_PATH = true;
                    showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER, !sharePref.getShowHiddenFileAndFolder(), Constant.SIZE_DESCENDING_ORDER);
                    break;
            }
        }
    }

    class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            if (item.getTitle().equals(getResources().getString(R.string.multi_select_delete))) {

                for (AbstractItem fileAndFolderAdapter : fileAndFolderFastAdapter.getSelectedItems()) {
                    if(((FileAndFolderAdapter) fileAndFolderAdapter).fileAndFolder.isFile()){
                        if (((FileAndFolderAdapter) fileAndFolderAdapter).fileAndFolder.delete()) {
                            Toast.makeText(MainActivity.this, ((FileAndFolderAdapter) fileAndFolderAdapter).fileAndFolder.getName() + " item is delete", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        CopyActivity.deleteFolder(((FileAndFolderAdapter) fileAndFolderAdapter).fileAndFolder);
                    }

                }
                mUndoHelper.remove(findViewById(android.R.id.content), "Item removed", "Undo", 1, fileAndFolderFastAdapter.getSelections());
            } else {
                if (fileAndFolderFastAdapter.getSelectedItems().size() > 0) {

                    ArrayList<FileAndFolderAdapter> fileAndFolderAdapters = new ArrayList<>();
                    List<String> files = new ArrayList<>();

                    for (AbstractItem fileAndFolderAdapter : fileAndFolderFastAdapter.getSelectedItems()) {
                        fileAndFolderAdapters.add(((FileAndFolderAdapter) fileAndFolderAdapter));
                        files.add(((FileAndFolderAdapter) fileAndFolderAdapter).fileAndFolder.getAbsolutePath());
                    }

                    JSONArray jsonArray = new JSONArray(files);
                    Intent intent = new Intent(MainActivity.this, CopyActivity.class);
                    intent.putExtra(Constant.PATH, jsonArray.toString());
                    startActivity(intent);
                }

            }
            //as we no longer have a selection so the actionMode can be finished
            mode.finish();
            //we consume the event
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }

}