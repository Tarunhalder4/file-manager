package com.example.filemanagers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLufickFileManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getAvailableMemory();

        mainAdapterFastItemAdapter = new FastItemAdapter<>();
        pieAdapterFastItemAdapter = new FastItemAdapter<>();
        bookmarkAdapterFastItemAdapter = new FastItemAdapter<>();
        containsInternalFastItemAdapter = new FastItemAdapter<>();
        internalStorageAdapterFastItemAdapter = new FastItemAdapter<>();

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

        internalStorageAdapterFastItemAdapter.add(new InternalStorageAdapter("4.68","17.30","22.30","75"));

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

        mainAdapterFastItemAdapter.withOnClickListener(new OnClickListener<MainAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<MainAdapter> adapter, MainAdapter item, int position) {
                Log.d(TAG, "onClick: main");
                Intent intent = new Intent(LufickFileManager.this,MainActivity.class);
                intent.putExtra(Constant.PATH,Constant.INTERNAL_STORAGE_PATH);
                startActivity(intent);
                return false;
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

    private void getAvailableMemory(){
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;
        bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long megAvailable = bytesAvailable / (1024 * 1024);
        Log.d("tag","Available MB : "+megAvailable);
        Log.d(TAG,"getTotalBytes: "+stat.getTotalBytes());
        Log.d(TAG, "getAvailableBlocksLong: "+stat.getAvailableBlocksLong());
        Log.d(TAG, "getFreeBlocksLong: "+stat.getFreeBlocksLong());
        long totalbyte = stat.getTotalBytes();
        Log.d(TAG, "use memory: "+(totalbyte-bytesAvailable));

    }

}