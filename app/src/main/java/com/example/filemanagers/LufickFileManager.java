package com.example.filemanagers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.example.filemanagers.databinding.ActivityLufickFileManagerBinding;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

public class LufickFileManager extends AppCompatActivity {

    ActivityLufickFileManagerBinding binding;
    FastItemAdapter<MainAdapter> mainAdapterFastItemAdapter;

    FastItemAdapter<PieAdapter> pieAdapterFastItemAdapter;
    FastItemAdapter<BookmarkAdapter> bookmarkAdapterFastItemAdapter;
    FastItemAdapter<BookmarkAdapter> containsInternalFastItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLufickFileManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainAdapterFastItemAdapter = new FastItemAdapter<>();
        pieAdapterFastItemAdapter = new FastItemAdapter<>();
        bookmarkAdapterFastItemAdapter = new FastItemAdapter<>();
        containsInternalFastItemAdapter = new FastItemAdapter<>();

        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.red_dot,"Audio","41 MB"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.magenta_dot,"Image","538 MB"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.blue_dot,"APK","26 MB"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.pink_dot_24,"video","2.1 GB"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.green_dot,"Apps","3.2 GB"));
        pieAdapterFastItemAdapter.add(new PieAdapter(R.drawable.yellow_dot,"Doc","40 MB"));

        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.camera_24,"Photo",Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.download_24,"Download",Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.safe_box_24,"Safe Box",Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.music_24,"Music",Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.watch_later_24,"Recent File",Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.file_24,"Documents",Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.app_24,"App Manager",Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.video_24,"video",Constant.CONTAINS_INTERNAL_STORAGE));
        containsInternalFastItemAdapter.add(new BookmarkAdapter(R.drawable.add_box_24,"Add to quick\naccess",Constant.CONTAINS_INTERNAL_STORAGE));

        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.empty_folder_48,"Download",Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.empty_folder_48,"Download",Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.empty_folder_48,"Download",Constant.BOOKMARK));
        bookmarkAdapterFastItemAdapter.add(new BookmarkAdapter(R.drawable.empty_folder_48,"Download",Constant.BOOKMARK));

        mainAdapterFastItemAdapter.add(new MainAdapter(pieAdapterFastItemAdapter,Constant.PIE_CHAT,LufickFileManager.this));
        mainAdapterFastItemAdapter.add(new MainAdapter(containsInternalFastItemAdapter,LufickFileManager.this,Constant.CONTAINS_INTERNAL_STORAGE));
        mainAdapterFastItemAdapter.add(new MainAdapter("5.68","17.30","22.98","75",Constant.INTERNAL_STORAGE));
        mainAdapterFastItemAdapter.add(new MainAdapter(bookmarkAdapterFastItemAdapter,LufickFileManager.this,Constant.BOOKMARK));

        binding.rec.setLayoutManager(new LinearLayoutManager(LufickFileManager.this));
        binding.rec.setAdapter(mainAdapterFastItemAdapter);

    }
}