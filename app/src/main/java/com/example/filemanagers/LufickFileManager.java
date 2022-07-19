package com.example.filemanagers;

import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.filemanagers.adapter.BookmarkAdapter;
import com.example.filemanagers.adapter.DrawableItemAdapter;
import com.example.filemanagers.adapter.DrawerExpendableAdapter;
import com.example.filemanagers.adapter.InternalStorageAdapter;
import com.example.filemanagers.adapter.MainAdapter;
import com.example.filemanagers.adapter.PieAdapter;
import com.example.filemanagers.databinding.ActivityLufickFileManagerBinding;

import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.expandable.ExpandableExtension;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.utilities.DragDropUtil;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialize.holder.StringHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class LufickFileManager extends AppCompatActivity {

    private ActivityLufickFileManagerBinding binding;
    private FastItemAdapter<MainAdapter> mainAdapterFastItemAdapter;

    private FastItemAdapter<PieAdapter> pieAdapterFastItemAdapter;
    private FastItemAdapter<BookmarkAdapter> containsInternalFastItemAdapter;
    private FastItemAdapter<InternalStorageAdapter> internalStorageAdapterFastItemAdapter;
    private FastItemAdapter<BookmarkAdapter> bookmarkAdapterFastItemAdapter;

    private SimpleDragCallback simpleDragCallback;
    private ItemTouchHelper touchHelper;
    private String TAG = "tag";

    private String totalMemory, useOfMemory, availableMemory, present;
    private Drawer drawer = null;
    private RecyclerView drawerRec = null;

    private FastItemAdapter<IItem> drawerFastItemAdapter;
    private ExpandableExtension<IItem> expandableExtension;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLufickFileManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.dashboard_title);

        LayoutInflater inflater = getLayoutInflater();
        View myLayout = inflater.inflate(R.layout.drawable_layout, binding.getRoot(), false);

        drawerRec = myLayout.findViewById(R.id.drawer_rec);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withHasStableIds(true)
                .withSavedInstance(savedInstanceState)
                .withToolbar(binding.toolbar)
                .withHeaderPadding(true)
                .withSliderBackgroundColorRes(R.color.black)
                .withHeader(R.layout.drawable_layout)
                .withCustomView(myLayout)
                .build();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(LufickFileManager.this, R.color.folder_background_dark));

        mainAdapterFastItemAdapter = new FastItemAdapter<>();
        pieAdapterFastItemAdapter = new FastItemAdapter<>();
        bookmarkAdapterFastItemAdapter = new FastItemAdapter<>();
        containsInternalFastItemAdapter = new FastItemAdapter<>();
        internalStorageAdapterFastItemAdapter = new FastItemAdapter<>();

        drawerFastItemAdapter = new FastItemAdapter<>();
        drawerFastItemAdapter.withSelectable(true);
        expandableExtension = new ExpandableExtension<>();
        drawerFastItemAdapter.addExtension(expandableExtension);

        drawerFastItemAdapter.withOnClickListener(new OnClickListener<IItem>() {
            @Override
            public boolean onClick(View v, IAdapter<IItem> adapter, IItem item, int position) {

                if(adapter.getAdapterItem(position).getTag()==Constant.Home){
                    Log.d(TAG, "onClick: "+Constant.Home);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.INTERNAL_STORAGE_FILE_FOLDER){
                    Log.d(TAG, "onClick: "+Constant.INTERNAL_STORAGE_FILE_FOLDER);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.ADD_CLOUD_STORAGE){
                    Log.d(TAG, "onClick: "+Constant.ADD_CLOUD_STORAGE);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.FTP_SERVER){
                    Log.d(TAG, "onClick: "+Constant.FTP_SERVER);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.LAN){
                    Log.d(TAG, "onClick: "+Constant.LAN);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.TRASH){
                    Log.d(TAG, "onClick: "+Constant.TRASH);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.SAFE_BOX_FOLDER){
                    Log.d(TAG, "onClick: "+Constant.SAFE_BOX_FOLDER);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.CONNECT_WITH_US){
                    Log.d(TAG, "onClick: "+Constant.CONNECT_WITH_US);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.APP_MANAGER_FOLDER){
                    Log.d(TAG, "onClick: "+Constant.APP_MANAGER_FOLDER);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.PHOTO_FOLDER){
                    Log.d(TAG, "onClick: "+Constant.PHOTO_FOLDER);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.VIDEO_FOLDER){
                    Log.d(TAG, "onClick: "+Constant.VIDEO_FOLDER);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.AUDIO_FILE){
                    Log.d(TAG, "onClick: "+Constant.AUDIO_FILE);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.DOCUMENTS_FOLDER){
                    Log.d(TAG, "onClick: "+Constant.DOCUMENTS_FOLDER);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.APK){
                    Log.d(TAG, "onClick: "+Constant.APK);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.COMPRESS_FOLDER){
                    Log.d(TAG, "onClick: "+Constant.COMPRESS_FOLDER);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.AAD_TO_QUICK_ACCESS){
                    Log.d(TAG, "onClick: "+Constant.AAD_TO_QUICK_ACCESS);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.RECENT_FILE){
                    Log.d(TAG, "onClick: "+Constant.RECENT_FILE);
                    drawer.closeDrawer();
                }else if(adapter.getAdapterItem(position).getTag()==Constant.INFORMATION){
                Log.d(TAG, "onClick: "+Constant.INFORMATION);
                drawer.closeDrawer();
            }

                return false;
            }
        });

        Constant.requestPermission(LufickFileManager.this);

        drawerRec.setLayoutManager(new LinearLayoutManager(this));
        //binding.rec.setItemAnimator(new SlideDownAlphaAnimator());
        drawerRec.setAdapter(drawerFastItemAdapter);

        List<IItem> items = new ArrayList<>();
        items.add(new DrawerExpendableAdapter(new StringHolder("Home"),R.drawable.home_24).withTag(Constant.Home));
        items.add(new DrawerExpendableAdapter(new StringHolder("Internal Stroage"),R.drawable.mobile12).withTag(Constant.INTERNAL_STORAGE_FILE_FOLDER));

        DrawerExpendableAdapter cloud = new DrawerExpendableAdapter(new StringHolder("Clouds"),R.drawable.cloud_12);
        List<IItem> subClouds = new ArrayList<>();
        subClouds.add(new DrawableItemAdapter(new StringHolder("Add cloud Storage"),R.drawable.cloud_12).withTag(Constant.ADD_CLOUD_STORAGE));
        cloud.withSubItems(subClouds);
        items.add(cloud);

        DrawerExpendableAdapter collection = new DrawerExpendableAdapter(new StringHolder("Collections"),R.drawable.api_12);
        List<IItem> collections = new ArrayList<>();
        collections.add(new DrawableItemAdapter(new StringHolder("images"),R.drawable.camera_24).withTag(Constant.PHOTO_FOLDER));
        collections.add(new DrawableItemAdapter(new StringHolder("Videos"),R.drawable.video_24).withTag(Constant.VIDEO_FOLDER));
        collections.add(new DrawableItemAdapter(new StringHolder("Audio"),R.drawable.music_24).withTag(Constant.AUDIO_FILE));
        collections.add(new DrawableItemAdapter(new StringHolder("Documents"),R.drawable.file_24).withTag(Constant.DOCUMENTS_FOLDER));
        collections.add(new DrawableItemAdapter(new StringHolder("Apks"),R.drawable.android_8).withTag(Constant.APK));
        collections.add(new DrawableItemAdapter(new StringHolder("Compressed"),R.drawable.folder_zip_24).withTag(Constant.COMPRESS_FOLDER));
        collections.add(new DrawableItemAdapter(new StringHolder("Quick Access"),R.drawable.add_box_24).withTag(Constant.AAD_TO_QUICK_ACCESS));
        collections.add(new DrawableItemAdapter(new StringHolder("Recent Files"),R.drawable.watch_later_24).withTag(Constant.RECENT_FILE));
        collection.withSubItems(collections);
        items.add(collection);

        DrawerExpendableAdapter network = new DrawerExpendableAdapter(new StringHolder("Network"),R.drawable.network_12);
        List<IItem> networks = new ArrayList<>();
        networks.add(new DrawableItemAdapter(new StringHolder("FTP Server"),R.drawable.ftp_server_24).withTag(Constant.FTP_SERVER));
        networks.add(new DrawableItemAdapter(new StringHolder("Lan(SMB 2.0)"),R.drawable.lan_24).withTag(Constant.LAN));
        network.withSubItems(networks);
        items.add(network);

        DrawerExpendableAdapter more = new DrawerExpendableAdapter(new StringHolder("More"),R.drawable.more_12);
        List<IItem> mores = new ArrayList<>();
        mores.add(new DrawableItemAdapter(new StringHolder("Trash"),R.drawable.ic_baseline_delete_24).withTag(Constant.TRASH));
        mores.add(new DrawableItemAdapter(new StringHolder("Safe box"),R.drawable.safe_box_24).withTag(Constant.SAFE_BOX_FOLDER));
        mores.add(new DrawableItemAdapter(new StringHolder("Connect with us"),R.drawable.user_12).withTag(Constant.CONNECT_WITH_US));
        mores.add(new DrawableItemAdapter(new StringHolder("App Manager"),R.drawable.app_24).withTag(Constant.APP_MANAGER_FOLDER));
        more.withSubItems(mores);
        items.add(more);

        items.add(new DrawerExpendableAdapter(new StringHolder("Information"),R.drawable.information_whight_24).withTag(Constant.INFORMATION));

        drawerFastItemAdapter.add(items);


        mainAdapterFastItemAdapter = new FastItemAdapter<>();
        pieAdapterFastItemAdapter = new FastItemAdapter<>();
        bookmarkAdapterFastItemAdapter = new FastItemAdapter<>();
        containsInternalFastItemAdapter = new FastItemAdapter<>();
        internalStorageAdapterFastItemAdapter = new FastItemAdapter<>();

        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Log.e(TAG, "call: " +Thread.currentThread().getName() );

                setItemInPieAdapter();
                setBookmarkAdapter();
                setItemInContainsInternalAdapter();
                getMemoryInformation();
                setInternalMemoryInformation();
                setMainAdapter();
                binding.rec.setLayoutManager(new LinearLayoutManager(LufickFileManager.this));
                binding.rec.setAdapter(mainAdapterFastItemAdapter);


                mainAdapterFastItemAdapter.getAdapterItem(2).withSelectable(true);

                mainAdapterFastItemAdapter.getAdapterItem(2).withOnItemClickListener(new OnClickListener<MainAdapter>() {
                    @Override
                    public boolean onClick(View v, IAdapter<MainAdapter> adapter, MainAdapter item, int position) {
                        Intent intent = new Intent(LufickFileManager.this, MainActivity.class);
                        intent.putExtra(Constant.PATH, Constant.INTERNAL_STORAGE_PATH);
                        startActivity(intent);
                        return true;
                    }
                });


                Log.e(TAG, "call: " );
                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                Log.e(TAG, "then: " + Thread.currentThread().getName());
                if (task.isCompleted()){
                    Log.e(TAG, "then: "+ task + "is finish");
                }

                if(task.isCancelled()){
                    Log.e(TAG, "then: " + task +"cancel ");
                }
                return null;
            }
        },Task.UI_THREAD_EXECUTOR);



        simpleDragCallback = new SimpleDragCallback(new ItemTouchCallback() {
            @Override
            public boolean itemTouchOnMove(int oldPosition, int newPosition) {
                DragDropUtil.onMove(mainAdapterFastItemAdapter.getItemAdapter(), oldPosition, newPosition);
                return true;
            }

            @Override
            public void itemTouchDropped(int oldPosition, int newPosition) {

            }
        });

        touchHelper = new ItemTouchHelper(simpleDragCallback);
        touchHelper.attachToRecyclerView(binding.rec);

    }

    @SuppressLint("DefaultLocale")
    private void getMemoryInformation() {
        long tempTotalMemory, tempUseOfMemory, tempAvailableMemory, tempPresent;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long megaBytes = 1000 * 1000;
        long gigaBytes = megaBytes * 1000;

        tempTotalMemory = stat.getTotalBytes();
        tempUseOfMemory = (tempTotalMemory - bytesAvailable);
        tempAvailableMemory = (tempTotalMemory - tempUseOfMemory);

        tempPresent = (tempUseOfMemory * 100) / tempTotalMemory;

        present = String.valueOf(tempPresent);
        totalMemory = String.format("%.2f", (float) tempTotalMemory / gigaBytes);
        useOfMemory = String.format("%.2f", (float) tempUseOfMemory / gigaBytes);
        availableMemory = String.format("%.2f", (float) tempAvailableMemory / gigaBytes);
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    void setItemInPieAdapter(){
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.red_dot, "Audio", "1"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.magenta_dot, "Image", "2"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.blue_dot, "APK", "3"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.pink_dot_24, "video", "4"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.green_dot, "Apps", "5"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.yellow_dot, "Doc", "6"));
    }

    void setItemInContainsInternalAdapter(){
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.camera_24, Constant.PHOTO_FOLDER, Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.download_24, Constant.DOWNLOAD_FOLDER, Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.safe_box_24, Constant.SAFE_BOX_FOLDER, Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.music_24, Constant.MUSIC_FOLDER, Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.watch_later_24, Constant.RECENT_FILE, Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.file_24, Constant.DOCUMENTS_FOLDER, Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.app_24, Constant.APP_MANAGER_FOLDER, Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.video_24, Constant.VIDEO_FOLDER, Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.add_box_24, "Add to quick\naccess", Constant.CONTAINS_INTERNAL_STORAGE));
    }

    void setBookmarkAdapter(){
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.fill_folder_48, Constant.DOWNLOAD_FOLDER, Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.dcim_camera_24, Constant.DCIM_FOLDER, Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.movies_24, Constant.MOVIES_FOLDER, Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.pictures_24, Constant.PICTURES_FOLDER, Constant.BOOKMARK));
    }

    void setInternalMemoryInformation(){
        internalStorageAdapterFastItemAdapter.add(new InternalStorageAdapter(availableMemory, useOfMemory, totalMemory, present));
    }

    void setMainAdapter(){
        internalStorageAdapterFastItemAdapter.add(new InternalStorageAdapter(availableMemory, useOfMemory, totalMemory, present));
        mainAdapterFastItemAdapter.add(new MainAdapter(pieAdapterFastItemAdapter, Constant.PIE_CHAT, LufickFileManager.this));
        mainAdapterFastItemAdapter.add(new MainAdapter(containsInternalFastItemAdapter, LufickFileManager.this, Constant.CONTAINS_INTERNAL_STORAGE));
        mainAdapterFastItemAdapter.add(new MainAdapter(internalStorageAdapterFastItemAdapter, Constant.INTERNAL_STORAGE));
        mainAdapterFastItemAdapter.add(new MainAdapter(bookmarkAdapterFastItemAdapter, LufickFileManager.this, Constant.BOOKMARK));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode== Constant.REQUEST_CODE){
            if (grantResults.length > 0) {
                boolean WRITE_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean READ_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {

                    Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



}