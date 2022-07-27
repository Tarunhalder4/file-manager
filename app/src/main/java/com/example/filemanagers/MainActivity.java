package com.example.filemanagers;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;

import java.lang.annotation.Retention;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.example.filemanagers.adapter.PathAdapter;
import com.example.filemanagers.adapter.PhotoGridAdapter;
import com.example.filemanagers.databinding.ActivityMainBinding;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.ISelectionListener;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter.listeners.OnLongClickListener;
import com.mikepenz.fastadapter.utils.ComparableItemListImpl;
import com.mikepenz.fastadapter_extensions.ActionModeHelper;
import com.mikepenz.fastadapter_extensions.UndoHelper;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialize.util.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    private FastAdapter<FileAndFolderAdapter> fileAndFolderFastAdapter;
    private FastItemAdapter<PhotoGridAdapter> photoGridAdapterFastItemAdapter;
    private FastItemAdapter<PathAdapter> pathAdapterFastItemAdapter;

    private ItemAdapter<FileAndFolderAdapter> fileAndFolderItemAdapter;

    private List<FileAndFolderAdapter> fileAndFolderAdapterList;
    private List<PhotoGridAdapter> photoGridAdapterList;
    private ArrayDeque<PathAdapter> pathAdapterArrayDeque;
    private List<PathAdapter> pathAdapterList;
    private List<File> selectableFile;

    private ComparableItemListImpl<FileAndFolderAdapter> comparableItemList;

    private ActionModeHelper<FileAndFolderAdapter> mActionModeHelper;
    private UndoHelper mUndoHelper;

    ArrayList<File> newFiles = null;

    @SortingStrategy
    private int sortingStrategy;

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

        comparableItemList = new ComparableItemListImpl<>(getComparator());

        fileAndFolderItemAdapter = new ItemAdapter<>(comparableItemList);
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
        selectableFile = new ArrayList<>();

        newFiles = new ArrayList<>();

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

                    showFileAndFolder(item.file, Constant.INTERNAL_STORAGE_FILE_FOLDER ,sharePref.getShowHiddenFileAndFolder());
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


        fileAndFolderFastAdapter.withSelectionListener(new ISelectionListener<FileAndFolderAdapter>() {
            @Override
            public void onSelectionChanged(FileAndFolderAdapter item, boolean selected) {
                Log.i("FastAdapter", "SelectedCount: " + fileAndFolderFastAdapter.getSelections().size() + " ItemsCount: " + fileAndFolderFastAdapter.getSelectedItems().size());
                // Set<FileAndFolderAdapter> fileAndFolderAdapters = fileAndFolderFastAdapter.getSelectedItems();
            }
        });

        fileAndFolderFastAdapter.withOnPreClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, @NonNull FileAndFolderAdapter item, int position) {
                //we handle the default onClick behavior for the actionMode. This will return null if it didn't do anything and you can handle a normal onClick
                Boolean res = mActionModeHelper.onClick(item);
                return res != null ? res : false;
            }
        });

        fileAndFolderFastAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {
                if (!Constant.LOG_CLICK_ACTIVATED) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    if (item.fileAndFolder.isDirectory()) {
                        binding.noFileAvailable.setVisibility(View.GONE);
                        path = item.fileAndFolder.getPath();
                        peekPath = true;
                        showFileAndFolder(item.fileAndFolder, Constant.INTERNAL_STORAGE_FILE_FOLDER ,sharePref.getShowHiddenFileAndFolder());
                    } else {
                        if (item.fileAndFolder.getName().endsWith(".pdf")) {
                            openFile(item.fileAndFolder, Constant.PDF_FILE);
                        }

                        if (item.fileAndFolder.getName().endsWith(".jpg") || item.fileAndFolder.getName().endsWith(".png")) {
                            openFile(item.fileAndFolder, Constant.PHOTO_FILE);
                        }

                        if (item.fileAndFolder.getName().endsWith(".mp3")) {
                            openFile(item.fileAndFolder, Constant.AUDIO_FILE);
                        }

                        if (item.fileAndFolder.getName().endsWith(".mp4")) {
                            openFile(item.fileAndFolder, Constant.VIDEO_FILE);
                        }

                        if (item.fileAndFolder.isDirectory()) {
                            showFileAndFolder(item.fileAndFolder, Constant.INTERNAL_STORAGE_FILE_FOLDER ,sharePref.getShowHiddenFileAndFolder());
                        }

                    }
                } else {
                    if (fileAndFolderFastAdapter.getSelectedItems().size() == 0) {
                        Constant.LOG_CLICK_ACTIVATED = false;
                    }
                    Toast.makeText(v.getContext(), "SelectedCount: " + fileAndFolderFastAdapter.getSelections().size() + " ItemsCount: " + fileAndFolderFastAdapter.getSelectedItems().size(), Toast.LENGTH_SHORT).show();
                    Set<FileAndFolderAdapter> fileAndFolderAdapters = fileAndFolderFastAdapter.getSelectedItems();
                }
                return false;
            }
        });

        mUndoHelper = new UndoHelper<>(fileAndFolderFastAdapter, new UndoHelper.UndoListener<FileAndFolderAdapter>() {
            @Override
            public void commitRemove(Set<Integer> positions, ArrayList<FastAdapter.RelativeInfo<FileAndFolderAdapter>> removed) {
                Log.e("UndoHelper", "Positions: " + positions.toString() + " Removed: " + removed.size());
            }
        });

        fileAndFolderFastAdapter.withOnPreLongClickListener(new OnLongClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onLongClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {
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

    @SortingStrategy
    int toSortingStrategy(int val) {
        return val;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the adapter to the bundle
        outState = fileAndFolderFastAdapter.saveInstanceState(outState);
        //We need to persist our sorting strategy between orientation changes
        outState.putInt("sorting_strategy", sortingStrategy);
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
        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.PHOTO_FILE)) {
            File file = Environment.getExternalStorageDirectory();
            showPath(file);
//            Task.callInBackground(new Callable<Object>() {
//                @Override
//                public Object call() throws Exception {
//                    showPhotoFolder(file);
//                    return null;
//                }
//            });
            showPhotoFolder(file);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DOWNLOAD_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER ,!sharePref.getShowHiddenFileAndFolder());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.SAFE_BOX_FOLDER)) {
            Toast.makeText(MainActivity.this, Constant.SAFE_BOX_FOLDER, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.MUSIC_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.AUDIO_FILE ,!sharePref.getShowHiddenFileAndFolder());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.RECENT_FILE)) {
            Toast.makeText(MainActivity.this, Constant.RECENT_FILE, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DOCUMENTS_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.DOCUMENTS_FILE ,!sharePref.getShowHiddenFileAndFolder());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.APP_MANAGER_FOLDER)) {
            Toast.makeText(MainActivity.this, Constant.APP_MANAGER_FOLDER, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.VIDEO_FOLDER)) {
            File file = Environment.getExternalStorageDirectory();
            showFileAndFolder(file, Constant.VIDEO_FILE,!sharePref.getShowHiddenFileAndFolder());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.AAD_TO_QUICK_ACCESS)) {
            Toast.makeText(MainActivity.this, Constant.AAD_TO_QUICK_ACCESS, Toast.LENGTH_SHORT).show();
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.INTERNAL_STORAGE_PATH)) {
            path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER ,!sharePref.getShowHiddenFileAndFolder());
            backCount = 1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.FOLDER_PATH)) {
            String path = getIntent().getStringExtra(Constant.FOLDER_PATH);
            File file = new File(path);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER,!sharePref.getShowHiddenFileAndFolder());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DCIM_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER,!sharePref.getShowHiddenFileAndFolder());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.PICTURES_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER,!sharePref.getShowHiddenFileAndFolder());
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.MOVIES_FOLDER)) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER,!sharePref.getShowHiddenFileAndFolder());
            backCount = -1;
        }
    }


    private void showFileAndFolder(File mainFile, String requiredFile, boolean hide ) {

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
                    } else {
                        fileScanBySuffix(file, Constant.PHOTO_FILE);
                    }
                }
            } else if (requiredFile.equals(Constant.VIDEO_FILE)) {

                for (File file : filesAndFolders) {
                    if (file.isDirectory()) {
                        scanDirectory(file, Constant.VIDEO_FILE);
                    } else {
                        fileScanBySuffix(file, Constant.VIDEO_FILE);
                    }
                }
            } else if (requiredFile.equals(Constant.INTERNAL_STORAGE_FILE_FOLDER)) {

                for (File file : Objects.requireNonNull(mainFile.listFiles())) {
                    if (file.isDirectory() && !file.getName().startsWith(".") && hide) {
                        fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    }else if(file.isDirectory() && !hide){
                        fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    }

                }

                for (File file : Objects.requireNonNull(mainFile.listFiles())) {
                    if (file.isFile() && !file.getName().startsWith(".") && hide) {
                        fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    }else if(file.isFile() && !hide){
                        fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    }

                }
            }

            fileAndFolderItemAdapter.add(fileAndFolderAdapterList);
            binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            binding.rec.setAdapter(fileAndFolderFastAdapter);


        } else {
            binding.noFileAvailable.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

    }


    void showPath(File file) {
        if (backPress) {
            pathAdapterFastItemAdapter.clear();
            pathAdapterList.clear();
            pathAdapterArrayDeque.pollLast();
            backPress = false;
        }else if(Constant.HIDE_UN_HIDE_RENAME){
            pathAdapterFastItemAdapter.clear();
            pathAdapterList.clear();
            Constant.HIDE_UN_HIDE_RENAME = false;
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
            //fileAndFolderAdapterFastItemAdapter.clear();
            fileAndFolderItemAdapter.clear();

            showFileAndFolder(parent, Constant.INTERNAL_STORAGE_FILE_FOLDER ,Constant.HIDE);

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
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER ,Constant.HIDE);
        } else if (event.isFileRename()) {
            String Path = event.getFilePath();
            File file = new File(Path);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER ,Constant.HIDE);
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


//    @Override
//    public void onOptionsMenuClosed(Menu menu) {
//        super.onOptionsMenuClosed(menu);
//        Log.e(TAG, "onOptionsMenuClosed: " );
//        MenuItem item = menu.findItem(R.id.show_hidden_folder_files);
//
//    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        Log.e(TAG, "onPrepareOptionsMenu: " );
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        Log.e(TAG, "onMenuOpened: " );
        MenuItem item = menu.findItem(R.id.show_hidden_folder_files);
        item.setChecked(sharePref.getShowHiddenFileAndFolder());
        return super.onMenuOpened(featureId, menu);
    }

    boolean itemClick = true;

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sort:
                sortingDialogBox();
                break;
            case R.id.create_file:

                break;
            case R.id.create_folder:
               // createFolder(path);
                break;
            case R.id.view_option:
                break;
            case R.id.close:
                Intent intent = new Intent(MainActivity.this,LufickFileManager.class);
                startActivity(intent);
                finish();
                break;
            case R.id.show_file_size:
                break;
            case R.id.show_folder_size:
                break;
            case R.id.show_full_name_of_files:
                break;
            case R.id.show_hidden_folder_files:
                if(item.isCheckable()) {
                    if (itemClick) {
                        Constant.HIDE_UN_HIDE_RENAME = true;
                        Log.e(TAG, "onOptionsItemSelected1: " + item.isCheckable());
                        sharePref.setShowHiddenFileAndFolder(true);
                        item.setChecked(true);
                        Log.e(TAG, "onOptionsItemSelected: "+path );
                        File file = new File(path);
                        showFileAndFolder(file,Constant.INTERNAL_STORAGE_FILE_FOLDER,false);
                        itemClick = false;
                    } else {
                        Constant.HIDE_UN_HIDE_RENAME = true;
                        Log.e(TAG, "onOptionsItemSelected: "+path );
                        Log.e(TAG, "onOptionsItemSelected2: " + item.isCheckable());
                        sharePref.setShowHiddenFileAndFolder(false);
                        item.setChecked(false);
                        File file = new File(path);
                        showFileAndFolder(file,Constant.INTERNAL_STORAGE_FILE_FOLDER,true);
                        itemClick = true;
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
        TextView save ,cancel, title;

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
                if(!fileName.getText().toString().trim().equals("")){
                    File destination;
                    String folderName = fileName.getText().toString().trim();
                    Log.e(TAG, "onClick: "+ path );
                    File file = new File(path);
                    //Creating a folder using mkdir() method
                    boolean bool = file.mkdir();
                    File current = new File(file.getAbsolutePath()+folderName);
                    if (bool) {

//                        EventMessage eventMessage = new EventMessage();
//                        eventMessage.setFileRename(true);
//                        eventMessage.setFilePath(FilePath);
//                        EventBus.getDefault().post(eventMessage);

                        String filePath=current.getParent();
                        if(filePath != null){
                            File currentFile = new File(filePath);
                            showFileAndFolder(currentFile, Constant.INTERNAL_STORAGE_FILE_FOLDER ,!sharePref.getShowHiddenFileAndFolder());
                            Toast.makeText(MainActivity.this, "Folder created", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Folder not created", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                }else{
                    Toast.makeText(MainActivity.this,"Please entry folder name",Toast.LENGTH_SHORT).show();
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



//        File file = new File(path);
//        //Creating a folder using mkdir() method
//        boolean bool = file.mkdir();
//        if (bool) {
//            showFileAndFolder(file.getParentFile(), Constant.INTERNAL_STORAGE_FILE_FOLDER);
//            Toast.makeText(MainActivity.this, "Folder create successfully", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(MainActivity.this, "Folder not create successfully", Toast.LENGTH_SHORT).show();
//        }
//    }


    private void createFile() {

    }


    private void multiselectDelete() {

        for (FileAndFolderAdapter fileAndFolderAdapter : fileAndFolderFastAdapter.getSelectedItems()) {
            if (fileAndFolderAdapter.fileAndFolder.delete()) {
                Toast.makeText(MainActivity.this, fileAndFolderAdapter.fileAndFolder.getName() + " file is delete", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, fileAndFolderAdapter.fileAndFolder.getName() + " file is delete", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void sortingDialogBox() {
        TextView ascendingOrder, descendingOrder;
        CheckBox checkBoxShowOnlyFolder;
        RadioGroup radioGroup;
        RadioButton nameSort, sizeSort, typeSort, dateSort;


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.sort_dialog_box, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        ascendingOrder = view.findViewById(R.id.sort_ascending);
        descendingOrder = view.findViewById(R.id.sort_descending);
        checkBoxShowOnlyFolder = view.findViewById(R.id.sort_only_this_folder);
        radioGroup = view.findViewById(R.id.radio_group);
        nameSort = view.findViewById(R.id.sort_name);


        ascendingOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.ASCENDING_ORDER = true;
                setAscendingAndDescendingOrder(sharePref.getSortId());
                Toast.makeText(MainActivity.this, "ascending order", Toast.LENGTH_SHORT).show();
            }
        });

        descendingOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.ASCENDING_ORDER = false;
                setAscendingAndDescendingOrder(sharePref.getSortId());
                Toast.makeText(MainActivity.this, "ascending order", Toast.LENGTH_SHORT).show();
            }
        });


        int sortingId = sharePref.getSortId();
        if (sortingId != 0) {
            radioGroup.check(sortingId);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "onCheckedChanged: " + checkedId);
                Constant.checkedId = checkedId;
                setAscendingAndDescendingOrder(checkedId);
                dialog.dismiss();
            }
        });

    }

    private void setAscendingAndDescendingOrder(int checkedId) {
        Log.d(TAG, "setAscendingAndDescendingOrder: " + checkedId);
        sharePref.setSortId(checkedId);
        if (Constant.ASCENDING_ORDER) {
            switch (checkedId) {
                case Constant.ID_SORT_NAME:
                    sortingStrategy = Constant.NAME_ASCENDING_ORDER;
                    comparableItemList.withComparator(getComparator());
                    break;
                case Constant.ID_SORT_DATE:
                    sortingStrategy = Constant.DATE_ASCENDING_ORDER;
                    comparableItemList.withComparator(getComparator());
                    break;
                case Constant.ID_SORT_SIZE:
                    sortingStrategy = Constant.SIZE_ASCENDING_ORDER;
                    comparableItemList.withComparator(getComparator());
                    break;
            }
        } else {
            switch (checkedId) {
                case Constant.ID_SORT_NAME:
                    sortingStrategy = Constant.NAME_DESCENDING_ORDER;
                    comparableItemList.withComparator(getComparator());
                    break;
                case Constant.ID_SORT_DATE:
                    sortingStrategy = Constant.DATE_DESCENDING_ORDER;
                    comparableItemList.withComparator(getComparator());
                    break;
                case Constant.ID_SORT_SIZE:
                    sortingStrategy = Constant.SIZE_DESCENDING_ORDER;
                    comparableItemList.withComparator(getComparator());
                    break;
            }
        }
    }

    @NonNull
    private Comparator<FileAndFolderAdapter> getComparator() {
        switch (sortingStrategy) {
            case Constant.NAME_ASCENDING_ORDER:
                return new NameAscending();
            case Constant.NAME_DESCENDING_ORDER:
                return new NameDescending();
            case Constant.SIZE_ASCENDING_ORDER:
                return new SizeAscending();
            case Constant.SIZE_DESCENDING_ORDER:
                return new SizeDescending();
            case Constant.DATE_ASCENDING_ORDER:
                return new DateAscending();
            case Constant.DATE_DESCENDING_ORDER:
                return new DateDescending();
        }

        throw new RuntimeException("This sortingStrategy is not supported.");
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Constant.NAME_ASCENDING_ORDER, Constant.NAME_DESCENDING_ORDER,
            Constant.DATE_ASCENDING_ORDER, Constant.DATE_DESCENDING_ORDER,
            Constant.SIZE_ASCENDING_ORDER, Constant.SIZE_DESCENDING_ORDER})
    public @interface SortingStrategy {
    }


    private class NameAscending implements Comparator<FileAndFolderAdapter>, Serializable {
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return o1.fileAndFolder.getName().compareTo(o2.fileAndFolder.getName());
        }
    }

    private class NameDescending implements Comparator<FileAndFolderAdapter>, Serializable {
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return o2.fileAndFolder.getName().compareTo(o1.fileAndFolder.getName());
        }
    }

    private class DateAscending implements Comparator<FileAndFolderAdapter>, Serializable {
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return String.valueOf(o1.fileAndFolder.lastModified()).compareTo(String.valueOf(o2.fileAndFolder.lastModified()));
        }
    }

    private class DateDescending implements Comparator<FileAndFolderAdapter>, Serializable {
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return String.valueOf(o2.fileAndFolder.lastModified()).compareTo(String.valueOf(o1.fileAndFolder.lastModified()));
        }
    }

    private class SizeAscending implements Comparator<FileAndFolderAdapter>, Serializable {
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return String.valueOf(o1.fileAndFolder.length()).compareTo(String.valueOf(o2.fileAndFolder.length()));
        }
    }

    private class SizeDescending implements Comparator<FileAndFolderAdapter>, Serializable {
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return Arrays.toString(o1.fileAndFolder.listFiles()).compareTo(Arrays.toString(o2.fileAndFolder.listFiles()));
        }
    }

    class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            if (item.getTitle().equals(getResources().getString(R.string.multi_select_delete))) {
                for (FileAndFolderAdapter fileAndFolderAdapter : fileAndFolderFastAdapter.getSelectedItems()) {
                    if (fileAndFolderAdapter.fileAndFolder.delete()) {
                        Toast.makeText(MainActivity.this, fileAndFolderAdapter.fileAndFolder.getName() + " item is delete", Toast.LENGTH_SHORT).show();
                    }
                }
                mUndoHelper.remove(findViewById(android.R.id.content), "Item removed", "Undo", 1, fileAndFolderFastAdapter.getSelections());
            } else {
                if (fileAndFolderFastAdapter.getSelectedItems().size() > 0) {
                    ArrayList<FileAndFolderAdapter> fileAndFolderAdapters = new ArrayList<>();
                    List<String> files = new ArrayList<>();
                    for (FileAndFolderAdapter fileAndFolderAdapter : fileAndFolderFastAdapter.getSelectedItems()) {
                        fileAndFolderAdapters.add(fileAndFolderAdapter);
                        files.add(fileAndFolderAdapter.fileAndFolder.getAbsolutePath());
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