package com.example.filemanagers;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CopyFragment extends Fragment {

    RecyclerView recyclerView;
    TextView noFileAvailable;
    private String TAG = "tag";
    private FastItemAdapter<FileAndFolderAdapter> fastItemAdapter;
    ArrayList<File> newFiles;

    public CopyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);
        newFiles = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_copy, container, false);
        noFileAvailable =view.findViewById(R.id.no_file_available_fragment);
        recyclerView = view.findViewById(R.id.copy_rec);

        File file = Environment.getExternalStorageDirectory();
        showFileAndFolder(file);


        fastItemAdapter.withOnClickListener(new OnClickListener<FileAndFolderAdapter>() {
            @Override
            public boolean onClick(View v, IAdapter<FileAndFolderAdapter> adapter, FileAndFolderAdapter item, int position) {

                if(item.fileAndFolder.isDirectory()){
                    String path = item.fileAndFolder.getPath();
                    File file = new File(path);
                    showFileAndFolder(file);
                }
                return true;
            }
        });

        return view;
    }


    private void showFileAndFolder(File mainFile){
        fastItemAdapter.clear();
        List<File> filesAndFolders = Arrays.asList(Objects.requireNonNull(mainFile.listFiles()));
        if (filesAndFolders.size() == 0){
            noFileAvailable.setVisibility(View.VISIBLE);
            Log.d(TAG, "empty folder");
        }else {
          noFileAvailable.setVisibility(View.GONE);
            List<FileAndFolderAdapter> fileAndFolderAdapters = new ArrayList<>();

            for (File file : filesAndFolders){
                if(file.isDirectory()){
                    fileAndFolderAdapters.add(new FileAndFolderAdapter(file,getActivity(),null));
                    fastItemAdapter.add(fileAndFolderAdapters);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(fastItemAdapter);
                }
            }

        }
    }

}