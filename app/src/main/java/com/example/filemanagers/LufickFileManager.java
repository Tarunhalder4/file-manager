package com.example.filemanagers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.filemanagers.adapter.BookmarkAdapter;
import com.example.filemanagers.adapter.InternalStorageAdapter;
import com.example.filemanagers.adapter.MainAdapter;
import com.example.filemanagers.adapter.PieAdapter;
import com.example.filemanagers.databinding.ActivityLufickFileManagerBinding;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.utilities.DragDropUtil;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import java.util.Objects;

public class LufickFileManager extends AppCompatActivity {

    private ActivityLufickFileManagerBinding binding;
    private FastItemAdapter<MainAdapter> mainAdapterFastItemAdapter;

    private FastItemAdapter<PieAdapter> pieAdapterFastItemAdapter;
    private FastItemAdapter<BookmarkAdapter> containsInternalFastItemAdapter;
    private FastItemAdapter<InternalStorageAdapter> internalStorageAdapterFastItemAdapter;
    private FastItemAdapter<BookmarkAdapter> bookmarkAdapterFastItemAdapter;

    private SimpleDragCallback simpleDragCallback;
    private ItemTouchHelper touchHelper;
    private String TAG ="tag";

    private String totalMemory,useOfMemory,availableMemory,present;
    private Drawer drawer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLufickFileManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.dashboard_title);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withHasStableIds(true)
                .withSavedInstance(savedInstanceState)
                .withToolbar(binding.toolbar)
                .withSliderBackgroundColor(Color.BLACK)
                .build();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(LufickFileManager.this,R.color.folder_background_dark));

        mainAdapterFastItemAdapter = new FastItemAdapter<>();
        pieAdapterFastItemAdapter = new FastItemAdapter<>();
        bookmarkAdapterFastItemAdapter = new FastItemAdapter<>();
        containsInternalFastItemAdapter = new FastItemAdapter<>();
        internalStorageAdapterFastItemAdapter = new FastItemAdapter<>();

        Constant.requestPermission(LufickFileManager.this);

        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.red_dot,"Audio","1"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.magenta_dot,"Image","2"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.blue_dot,"APK","3"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.pink_dot_24,"video","4"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.green_dot,"Apps","5"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.yellow_dot,"Doc","6"));

        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.camera_24,Constant.PHOTO_FOLDER,Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.download_24,Constant.DOWNLOAD_FOLDER,Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.safe_box_24,Constant.SAFE_BOX_FOLDER,Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.music_24,Constant.MUSIC_FOLDER,Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.watch_later_24,Constant.RECENT_FILE,Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.file_24,Constant.DOCUMENTS_FOLDER,Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.app_24,Constant.APP_MANAGER_FOLDER,Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.video_24,Constant.VIDEO_FOLDER,Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.add_box_24,"Add to quick\naccess",Constant.CONTAINS_INTERNAL_STORAGE));

        getMemoryInformation();
        internalStorageAdapterFastItemAdapter.add(new InternalStorageAdapter(availableMemory,useOfMemory,totalMemory,present));

        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.fill_folder_48,Constant.DOWNLOAD_FOLDER,Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.dcim_camera_24,Constant.DCIM_FOLDER,Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.movies_24,Constant.MOVIES_FOLDER,Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.pictures_24,Constant.PICTURES_FOLDER,Constant.BOOKMARK));

        mainAdapterFastItemAdapter.add(new MainAdapter(pieAdapterFastItemAdapter,Constant.PIE_CHAT,LufickFileManager.this));
        mainAdapterFastItemAdapter.add(new MainAdapter(containsInternalFastItemAdapter,LufickFileManager.this,Constant.CONTAINS_INTERNAL_STORAGE));
        mainAdapterFastItemAdapter.add(new MainAdapter(internalStorageAdapterFastItemAdapter,Constant.INTERNAL_STORAGE));
        mainAdapterFastItemAdapter.add(new MainAdapter(bookmarkAdapterFastItemAdapter,LufickFileManager.this,Constant.BOOKMARK));

        binding.rec.setLayoutManager(new LinearLayoutManager(LufickFileManager.this));
        binding.rec.setAdapter(mainAdapterFastItemAdapter);

        mainAdapterFastItemAdapter.getAdapterItem(2).withSelectable(true);

        mainAdapterFastItemAdapter.getAdapterItem(2).withOnItemClickListener(new OnClickListener<MainAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<MainAdapter> adapter, MainAdapter item, int position) {
                Intent intent = new Intent(LufickFileManager.this,MainActivity.class);
                intent.putExtra(Constant.PATH,Constant.INTERNAL_STORAGE_PATH);
                startActivity(intent);
                return true;
            }
        });

        simpleDragCallback = new SimpleDragCallback(new ItemTouchCallback() {
            @Override
            public boolean itemTouchOnMove(int oldPosition, int newPosition) {
                DragDropUtil.onMove(mainAdapterFastItemAdapter.getItemAdapter(),oldPosition,newPosition);
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
    private void getMemoryInformation(){
        long tempTotalMemory,tempUseOfMemory,tempAvailableMemory, tempPresent;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long megaBytes= 1000*1000;
        long gigaBytes=megaBytes*1000;

        tempTotalMemory = stat.getTotalBytes();
        tempUseOfMemory = (tempTotalMemory-bytesAvailable);
        tempAvailableMemory = (tempTotalMemory-tempUseOfMemory);

        tempPresent = (tempUseOfMemory*100)/tempTotalMemory;

        present = String.valueOf(tempPresent);
        totalMemory = String.format("%.2f", (float)tempTotalMemory/gigaBytes);
        useOfMemory = String.format("%.2f", (float)tempUseOfMemory/gigaBytes);
        availableMemory = String.format("%.2f", (float)tempAvailableMemory/gigaBytes);
    }

    @Override
    public void onBackPressed() {
        if(drawer!=null && drawer.isDrawerOpen()){
            drawer.closeDrawer();
        }else{
            super.onBackPressed();
        }
    }
}