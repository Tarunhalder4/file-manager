package com.example.filemanagers;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filemanagers.adapter.PathAdapter;
import com.example.filemanagers.adapter.PhotoGridAdapter;
import com.example.filemanagers.databinding.ActivityMainBinding;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter.utils.ComparableItemListImpl;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    private ComparableItemListImpl<FileAndFolderAdapter> comparableItemList;

    ArrayList<File> newFiles = null;
    private Drawer drawer = null;

    @SortingStrategy
    private int sortingStrategy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_files);
        sharePref=SharePref.getInstance(MainActivity.this);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withHasStableIds(true)
                .withSavedInstance(savedInstanceState)
                .withToolbar(binding.toolBar)
                .withSliderBackgroundColor(Color.BLACK)
                .build();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.folder_background_dark));

        comparableItemList = new ComparableItemListImpl<>(getComparator());

        fileAndFolderItemAdapter = new ItemAdapter<>(comparableItemList);
        fileAndFolderFastAdapter = FastAdapter.with(fileAndFolderItemAdapter);

        photoGridAdapterFastItemAdapter = new FastItemAdapter<>();
        pathAdapterFastItemAdapter = new FastItemAdapter<>();

        fileAndFolderFastAdapter.withSelectable(true);
        photoGridAdapterFastItemAdapter.withSelectable(true);
        pathAdapterFastItemAdapter.withSelectable(true);

        photoGridAdapterList = new ArrayList<>();
        pathAdapterArrayDeque = new ArrayDeque<>();
        pathAdapterList = new ArrayList<>();

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
                if (item.file.isDirectory()){
                    path = item.file.getAbsolutePath();
                    pathAdapterArrayDeque.clear();
                    pathAdapterList.clear();
                    pathAdapterFastItemAdapter.clear();

                    String[] arrOfStr = item.file.getPath().split("/");

                    File file = item.file.getParentFile();
                    for(int i=4; i<=arrOfStr.length-1;i++){
                        assert file != null;
                        pathAdapterArrayDeque.offerFirst(new PathAdapter(file));
                        file=file.getParentFile();

                    }

                    pathAdapterList.addAll(pathAdapterArrayDeque);
                    pathAdapterFastItemAdapter.add(pathAdapterList);
                    binding.pathRec.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL, false));
                    binding.pathRec.setAdapter(pathAdapterFastItemAdapter);

                    showFileAndFolder(item.file,Constant.INTERNAL_STORAGE_FILE_FOLDER);
                }
                return false;
            }
        });

        photoGridAdapterFastItemAdapter.withOnClickListener(new OnClickListener<PhotoGridAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<PhotoGridAdapter> adapter, PhotoGridAdapter item, int position) {

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

        fileAndFolderFastAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {
                binding.progressBar.setVisibility(View.VISIBLE);
                if (item.fileAndFolder.isDirectory()) {
                    binding.noFileAvailable.setVisibility(View.GONE);
                    path = item.fileAndFolder.getPath();
                    peekPath = true;
                    showFileAndFolder(item.fileAndFolder, Constant.INTERNAL_STORAGE_FILE_FOLDER);
                } else {
                    if (item.fileAndFolder.getName().endsWith(".pdf")) {
                        openFile(item.fileAndFolder, Constant.PDF_FILE);
                    }

                    if (item.fileAndFolder.getName().endsWith(".jpg")||item.fileAndFolder.getName().endsWith(".png")) {
                        openFile(item.fileAndFolder, Constant.PHOTO_FILE);
                    }

                    if (item.fileAndFolder.getName().endsWith(".mp3")) {
                        openFile(item.fileAndFolder, Constant.AUDIO_FILE);
                    }

                    if (item.fileAndFolder.getName().endsWith(".mp4")) {
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
            if (file.getName().endsWith(".jpg")||file.getName().endsWith(".png")) {
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
            showPhotoFolder(file);
            backCount = -1;
        }

        if (getIntent().getStringExtra(Constant.PATH).equals(Constant.DOWNLOAD_FOLDER)) {
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
        //fileAndFolderAdapterFastItemAdapter.clear();
        fileAndFolderItemAdapter.clear();
        showPath(mainFile);
        if (!Constant.checkPermission(MainActivity.this)) {
            binding.noFileAvailable.setText("Permission required for display file");
            binding.noFileAvailable.setVisibility(View.VISIBLE);
            binding.pathRec.setVisibility(View.GONE);
        }
        List<File> filesAndFolders = Arrays.asList(Objects.requireNonNull(mainFile.listFiles()));
        if (filesAndFolders.size() == 0) {
            binding.noFileAvailable.setVisibility(View.VISIBLE);
        } else {
            binding.noFileAvailable.setVisibility(View.GONE);
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

                for (File file : Objects.requireNonNull(mainFile.listFiles())){
                    if (file.isDirectory()) {
                        fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    }

                }

                for (File file : Objects.requireNonNull(mainFile.listFiles())){
                    if (file.isFile()) {
                        fileAndFolderAdapterList.add(new FileAndFolderAdapter(file, MainActivity.this, MainActivity.this));
                    }

                }
           }

            //fileAndFolderAdapterFastItemAdapter.add(fileAndFolderAdapterList);
            fileAndFolderItemAdapter.add(fileAndFolderAdapterList);
            binding.rec.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            binding.rec.setAdapter(fileAndFolderFastAdapter);

        }
        binding.progressBar.setVisibility(View.GONE);
    }

    void showPath(File file){
        if(backPress){
            pathAdapterFastItemAdapter.clear();
            pathAdapterList.clear();
            pathAdapterArrayDeque.pollLast();
            backPress=false;
        }else {
            pathAdapterFastItemAdapter.clear();
            pathAdapterList.clear();
            pathAdapterArrayDeque.offerLast(new PathAdapter(file));
        }

        pathAdapterList.addAll(pathAdapterArrayDeque);

        pathAdapterFastItemAdapter.add(pathAdapterList);
        binding.pathRec.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL, false));
        binding.pathRec.setAdapter(pathAdapterFastItemAdapter);

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

            showFileAndFolder(parent, Constant.INTERNAL_STORAGE_FILE_FOLDER);

            if (destinationPath.equals("/storage/emulated/0")) {
                backCount = 1;
            } else {
                backCount = 0;
            }

        } else {
            if(drawer!=null && drawer.isDrawerOpen()){
                drawer.closeDrawer();
                binding.progressBar.setVisibility(View.GONE);
            }else{
                super.onBackPressed();
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage event) {
        if (event.isDelete()) {
            String deletePath = event.getDeleteFilePath();
            File file = new File(deletePath);
            showFileAndFolder(file, Constant.INTERNAL_STORAGE_FILE_FOLDER);
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
                photoGridAdapterList.add(new PhotoGridAdapter(MainActivity.this,file));
                havefile = true;
            }else{
                if (!file1.getName().startsWith(".")&&file1.isDirectory()){
                    separatePhotoFolder(file1);
                }

            }
        }

    }


    void showPhotoFolder(File file){
        binding.progressBar.setVisibility(View.VISIBLE);
        separatePhotoFolder(file);
        photoGridAdapterFastItemAdapter.add(photoGridAdapterList);
        binding.rec.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
        binding.rec.setAdapter(photoGridAdapterFastItemAdapter);
        binding.progressBar.setVisibility(View.GONE);
    }

    void showPhotoInFolder(File file){
        photoGridAdapterList.clear();
        File[] files = file.listFiles();
        assert files != null;
        for (File file1:files){
            if(file1.getName().endsWith(".jpg")){
                photoGridAdapterList.add(new PhotoGridAdapter(MainActivity.this,file1));
            }
        }
        photoGridAdapterFastItemAdapter.add(photoGridAdapterList);
        binding.rec.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
        binding.rec.setAdapter(photoGridAdapterFastItemAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_file_menu, menu);
        menu.findItem(R.id.sort).setIcon(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_wrap_text).color(Color.BLACK));
        return super.onCreateOptionsMenu(menu);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.sort:
                sortingDialogBox();
                break;
            case R.id.create_file:
                break;
            case R.id.create_folder:
                break;
            case R.id.view_option:
                break;
            case R.id.close:
                break;
            case R.id.show_file_size:
                break;
            case R.id.show_folder_size:
                break;
            case R.id.show_full_name_of_files:
                break;
            case R.id.show_hidden_folder_files:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void sortingDialogBox(){
        TextView ascendingOrder, descendingOrder;
        CheckBox checkBoxShowOnlyFolder;
        RadioGroup radioGroup;
        RadioButton nameSort, sizeSort, typeSort, dateSort;


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.sort_dialog_box,null);
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
                Toast.makeText(MainActivity.this,"ascending order", Toast.LENGTH_SHORT).show();
            }
        });

        descendingOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.ASCENDING_ORDER =false;
                setAscendingAndDescendingOrder(sharePref.getSortId());
                Toast.makeText(MainActivity.this,"ascending order", Toast.LENGTH_SHORT).show();
            }
        });


        int sortingId = sharePref.getSortId();
        if(sortingId!=0){
            radioGroup.check(sortingId);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "onCheckedChanged: "+checkedId);
                Constant.checkedId=checkedId;
                setAscendingAndDescendingOrder(checkedId);
                dialog.dismiss();
            }
        });

    }

    private void setAscendingAndDescendingOrder(int checkedId){
        if(Constant.ASCENDING_ORDER){
            switch (checkedId){
                case Constant.ID_SORT_NAME:
                    sharePref.setSortId(checkedId);
                    sortingStrategy = Constant.NAME_ASCENDING_ORDER;
                    //Set the new comparator to the list
                    comparableItemList.withComparator(getComparator());
                    break;
                case Constant.ID_SORT_DATE:
                    sharePref.setSortId(checkedId);
                    sortingStrategy = Constant.DATE_ASCENDING_ORDER;
                    //Set the new comparator to the list
                    comparableItemList.withComparator(getComparator());
                    break;
                case Constant.ID_SORT_SIZE:
                    sharePref.setSortId(checkedId);
                    sortingStrategy = Constant.SIZE_ASCENDING_ORDER;
                    //Set the new comparator to the list
                    comparableItemList.withComparator(getComparator());
                    break;
            }
        }else {
            switch (checkedId){
                case Constant.ID_SORT_NAME:
                    sharePref.setSortId(checkedId);
                    sortingStrategy = Constant.NAME_DESCENDING_ORDER;
                    //Set the new comparator to the list
                    comparableItemList.withComparator(getComparator());
                    break;
                case Constant.ID_SORT_DATE:
                    sharePref.setSortId(checkedId);
                    sortingStrategy = Constant.DATE_DESCENDING_ORDER;
                    //Set the new comparator to the list
                    comparableItemList.withComparator(getComparator());
                    break;
                case Constant.ID_SORT_SIZE:
                    sharePref.setSortId(checkedId);
                    sortingStrategy = Constant.SIZE_DESCENDING_ORDER;
                    //Set the new comparator to the list
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
    @IntDef({ Constant.NAME_ASCENDING_ORDER,Constant.NAME_DESCENDING_ORDER,
    Constant.DATE_ASCENDING_ORDER,Constant.DATE_DESCENDING_ORDER,
    Constant.SIZE_ASCENDING_ORDER,Constant.SIZE_DESCENDING_ORDER})
    public @interface SortingStrategy {
    }


    private class NameAscending implements Comparator<FileAndFolderAdapter>, Serializable{
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return o1.fileAndFolder.getName().compareTo(o2.fileAndFolder.getName());
        }
    }

    private class NameDescending implements Comparator<FileAndFolderAdapter>, Serializable{
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return o2.fileAndFolder.getName().compareTo(o1.fileAndFolder.getName());
        }
    }

    private class DateAscending implements Comparator<FileAndFolderAdapter>, Serializable{
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return String.valueOf(o1.fileAndFolder.lastModified()).compareTo(String.valueOf(o2.fileAndFolder.lastModified()));
        }
    }

    private class DateDescending implements Comparator<FileAndFolderAdapter>, Serializable{
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return String.valueOf(o2.fileAndFolder.lastModified()).compareTo(String.valueOf(o1.fileAndFolder.lastModified()));
        }
    }

    private class SizeAscending implements Comparator<FileAndFolderAdapter>, Serializable{
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return String.valueOf(o1.fileAndFolder.length()).compareTo(String.valueOf(o2.fileAndFolder.length()));
        }
    }

    private class SizeDescending implements Comparator<FileAndFolderAdapter>, Serializable{
        @Override
        public int compare(FileAndFolderAdapter o1, FileAndFolderAdapter o2) {
            return Arrays.toString(o1.fileAndFolder.listFiles()).compareTo(Arrays.toString(o2.fileAndFolder.listFiles()));
        }
    }

}