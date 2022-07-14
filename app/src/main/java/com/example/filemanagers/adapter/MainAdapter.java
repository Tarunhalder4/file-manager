package com.example.filemanagers.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagers.Constant;
import com.example.filemanagers.FileAndFolderAdapter;
import com.example.filemanagers.MainActivity;
import com.example.filemanagers.R;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.utilities.DragDropUtil;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainAdapter extends AbstractItem<MainAdapter, MainAdapter.ViewHolder> {

    FastItemAdapter<PieAdapter> pieAdapterFastItemAdapter;
    FastItemAdapter<BookmarkAdapter> bookmarkAdapterFastItemAdapter;
    private int layoutType;
    Context context;
    /////////////////pie chat fields
    private SimpleDragCallback simpleDragCallback;
    private ItemTouchHelper touchHelper;

    ///////////////////internal Storage
    FastItemAdapter<InternalStorageAdapter> internalStorageAdapterFastItemAdapter;
    //////////////////bookmark
    boolean hide = true;

    ///////////pei contractor
    public MainAdapter(FastItemAdapter<PieAdapter> pieAdapterFastItemAdapter, int layoutType, Context context) {
        this.pieAdapterFastItemAdapter = pieAdapterFastItemAdapter;
        this.layoutType = layoutType;
        this.context = context;
    }

    ////////////bookmark contractor
    public MainAdapter(FastItemAdapter<BookmarkAdapter> bookmarkAdapterFastItemAdapter,Context context,int layoutType) {
        this.bookmarkAdapterFastItemAdapter = bookmarkAdapterFastItemAdapter;
        this.layoutType = layoutType;
        this.context = context;
    }

    //////////////internal storage contractor
    public MainAdapter(FastItemAdapter<InternalStorageAdapter> internalStorageAdapterFastItemAdapter, int layoutType) {
        this.internalStorageAdapterFastItemAdapter = internalStorageAdapterFastItemAdapter;
        this.layoutType = layoutType;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        if (layoutType== Constant.PIE_CHAT){
            return R.id.pie_chat_card_view_layout;
        }

        if (layoutType==Constant.CONTAINS_INTERNAL_STORAGE||layoutType==Constant.BOOKMARK){
            return R.id.bookmark_card_view_layout;
        }

        if (layoutType==Constant.INTERNAL_STORAGE){
            return R.id.internal_storage_card_view_layout;
        }
        return 0;
    }

    @Override
    public int getLayoutRes() {

        if (layoutType==Constant.PIE_CHAT){
            return R.layout.pie_chat_cardview;
        }

        if (layoutType==Constant.CONTAINS_INTERNAL_STORAGE||layoutType==Constant.BOOKMARK){
            return R.layout.bookmarks_cardview;
        }

        if (layoutType==Constant.INTERNAL_STORAGE){
            return R.layout.internal_storage_cardview;
        }

        return 0;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        if (layoutType==Constant.PIE_CHAT){
            holder.pieRecyclerView.setLayoutManager(new GridLayoutManager(context,2));
            holder.pieRecyclerView.setAdapter(pieAdapterFastItemAdapter);

            holder.pieChart.addPieSlice(
                    new PieModel(pieAdapterFastItemAdapter.getAdapterItem(0).fileType,
                           Float.parseFloat(pieAdapterFastItemAdapter.getAdapterItem(0).fileSize),
                            Color.parseColor("#F80202")));
            holder.pieChart.addPieSlice(
                    new PieModel(pieAdapterFastItemAdapter.getAdapterItem(1).fileType,
                            Float.parseFloat(pieAdapterFastItemAdapter.getAdapterItem(1).fileSize),
                            Color.parseColor("#5B26B6")));
            holder.pieChart.addPieSlice(
                    new PieModel(pieAdapterFastItemAdapter.getAdapterItem(2).fileType,
                            Float.parseFloat(pieAdapterFastItemAdapter.getAdapterItem(2).fileSize),
                            Color.parseColor("#0000FF")));
            holder.pieChart.addPieSlice(
                    new PieModel(pieAdapterFastItemAdapter.getAdapterItem(3).fileType,
                            Float.parseFloat(pieAdapterFastItemAdapter.getAdapterItem(3).fileSize),
                            Color.parseColor("#F3568B")));
            holder.pieChart.addPieSlice(
                    new PieModel(pieAdapterFastItemAdapter.getAdapterItem(4).fileType,
                            Float.parseFloat(pieAdapterFastItemAdapter.getAdapterItem(4).fileSize),
                            Color.parseColor("#05F40F")));
            holder.pieChart.addPieSlice(
                    new PieModel(pieAdapterFastItemAdapter.getAdapterItem(5).fileType,
                            Float.parseFloat(pieAdapterFastItemAdapter.getAdapterItem(5).fileSize),
                            Color.parseColor("#F1D803")));

            // To animate the pie chart
            holder.pieChart.startAnimation();

            simpleDragCallback = new SimpleDragCallback(new ItemTouchCallback() {
                @Override
                public boolean itemTouchOnMove(int oldPosition, int newPosition) {
                    DragDropUtil.onMove(pieAdapterFastItemAdapter.getItemAdapter(),oldPosition,newPosition);
                    return true;
                }

                @Override
                public void itemTouchDropped(int oldPosition, int newPosition) {

                }
            });

            touchHelper = new ItemTouchHelper(simpleDragCallback);
            touchHelper.attachToRecyclerView(holder.pieRecyclerView);

        }

        if (layoutType==Constant.CONTAINS_INTERNAL_STORAGE||layoutType==Constant.BOOKMARK){

                simpleDragCallback = new SimpleDragCallback(new ItemTouchCallback() {
                    @Override
                    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
                        DragDropUtil.onMove(bookmarkAdapterFastItemAdapter.getItemAdapter(),oldPosition,newPosition);
                        return true;
                    }

                    @Override
                    public void itemTouchDropped(int oldPosition, int newPosition) {

                    }
                });

                touchHelper = new ItemTouchHelper(simpleDragCallback);

                if (layoutType==Constant.CONTAINS_INTERNAL_STORAGE){
                    holder.bookmarks.setVisibility(View.INVISIBLE);
                    holder.hide.setVisibility(View.INVISIBLE);
                    holder.containsInternalStorage.setVisibility(View.VISIBLE);
                    holder.bookmarkRecycleView.setLayoutManager(new GridLayoutManager(context,2));
                    holder.bookmarkRecycleView.setAdapter(bookmarkAdapterFastItemAdapter);

                    touchHelper.attachToRecyclerView(holder.bookmarkRecycleView);

                    bookmarkAdapterFastItemAdapter.withSelectable(true);

                    bookmarkAdapterFastItemAdapter.withOnClickListener(new OnClickListener<BookmarkAdapter>() {
                        @Override
                        public boolean onClick(View v, IAdapter<BookmarkAdapter> adapter, BookmarkAdapter item, int position) {
                            ///////////////contain internal storage and SD card
                            if(Objects.equals(item.folderName, Constant.PHOTO_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.PHOTO_FILE);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.DOWNLOAD_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.DOWNLOAD_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.SAFE_BOX_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.SAFE_BOX_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.MUSIC_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.MUSIC_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.RECENT_FILE)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.RECENT_FILE);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.DOCUMENTS_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.DOCUMENTS_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.APP_MANAGER_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.APP_MANAGER_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.VIDEO_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.VIDEO_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName,Constant.AAD_TO_QUICK_ACCESS )){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.AAD_TO_QUICK_ACCESS);
                                context.startActivity(intent);
                            }

                            return true;
                        }
                    });

                }else {

                    bookmarkAdapterFastItemAdapter.withSelectable(true);

                    bookmarkAdapterFastItemAdapter.withOnClickListener(new OnClickListener<BookmarkAdapter>() {
                        @Override
                        public boolean onClick(View v, IAdapter<BookmarkAdapter> adapter, BookmarkAdapter item, int position) {

                            if(Objects.equals(item.folderName,Constant.DCIM_FOLDER )){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.DCIM_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName,Constant.PICTURES_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.PICTURES_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.DOWNLOAD_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.DOWNLOAD_FOLDER);
                                context.startActivity(intent);
                            }

                            if(Objects.equals(item.folderName, Constant.MOVIES_FOLDER)){
                                Intent intent = new Intent(context,MainActivity.class);
                                intent.putExtra(Constant.PATH,Constant.MOVIES_FOLDER);
                                context.startActivity(intent);
                            }

                            return true;
                        }
                    });

                    holder.bookmarks.setVisibility(View.VISIBLE);
                    holder.hide.setVisibility(View.VISIBLE);
                    holder.containsInternalStorage.setVisibility(View.INVISIBLE);
                    holder.bookmarkRecycleView.setLayoutManager(new GridLayoutManager(context,2));
                    holder.bookmarkRecycleView.setAdapter(bookmarkAdapterFastItemAdapter);

                    touchHelper.attachToRecyclerView(holder.bookmarkRecycleView);

                    holder.hide.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(hide){
                                holder.bookmarkRecycleView.setVisibility(View.GONE);
                                holder.hide.setText("Show");
                                holder.hide.setPadding(8,0,0,16);
                                hide=false;
                            }else {
                                holder.bookmarkRecycleView.setVisibility(View.VISIBLE);
                                holder.hide.setText("Hide");
                                holder.hide.setPadding(8,0,0,0);
                                hide=true;
                            }
                        }
                    });

//                    if(Objects.equals(item.folderName,Constant.DCIM_FOLDER )){
//                        Intent intent = new Intent(context,MainActivity.class);
//                        intent.putExtra(Constant.PATH,Constant.DCIM_FOLDER);
//                        context.startActivity(intent);
//                    }
//
//                    if(Objects.equals(item.folderName,Constant.PICTURES_FOLDER)){
//                        Intent intent = new Intent(context,MainActivity.class);
//                        intent.putExtra(Constant.PATH,Constant.PICTURES_FOLDER);
//                        context.startActivity(intent);
//                    }
                }
        }

        if (layoutType==Constant.INTERNAL_STORAGE){
            holder.availableStorage.setText("Available "+internalStorageAdapterFastItemAdapter.getAdapterItem(0).availableStorage+" GB");
            holder.useOf.setText(internalStorageAdapterFastItemAdapter.getAdapterItem(0).useOfValue+" GB Used of "+internalStorageAdapterFastItemAdapter.getAdapterItem(0).totalValue+" GB");
            holder.progressValue.setText(internalStorageAdapterFastItemAdapter.getAdapterItem(0).progressValue+"%");
            holder.progressBar.setProgress(Integer.parseInt(internalStorageAdapterFastItemAdapter.getAdapterItem(0).progressValue));
            ConstraintSet cs;
            cs = new ConstraintSet();
            cs.clone(holder.cl);
            cs.setHorizontalBias(R.id.view,  Float.parseFloat(internalStorageAdapterFastItemAdapter.getAdapterItem(0).progressValue)/100);
            cs.applyTo(holder.cl);

        }
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);

        if (layoutType==Constant.PIE_CHAT){
            holder.pieRecyclerView.setLayoutManager(new GridLayoutManager(context,3));
            holder.pieRecyclerView.setAdapter(null);
            holder.internal.setVisibility(View.INVISIBLE);
        }

        if (layoutType==Constant.CONTAINS_INTERNAL_STORAGE||layoutType==Constant.BOOKMARK){
            if (layoutType==Constant.CONTAINS_INTERNAL_STORAGE){
                holder.bookmarks.setVisibility(View.GONE);
                holder.hide.setVisibility(View.GONE);
                holder.containsInternalStorage.setVisibility(View.GONE);
                holder.bookmarkRecycleView.setLayoutManager(new GridLayoutManager(context,3));
                holder.bookmarkRecycleView.setAdapter(null);
            }else {
                holder.bookmarks.setVisibility(View.GONE);
                holder.hide.setVisibility(View.GONE);
                holder.containsInternalStorage.setVisibility(View.GONE);
                holder.bookmarkRecycleView.setLayoutManager(new GridLayoutManager(context,3));
                holder.bookmarkRecycleView.setAdapter(null);
            }
        }

        if (layoutType==Constant.INTERNAL_STORAGE){
            holder.availableStorage.setText(null);
            holder.useOf.setText(null);
            holder.progressValue.setText(null);
            holder.progressBar.setProgress(0);
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        //////////////////pie chat view
        PieChart pieChart;
        TextView internal;
        RecyclerView pieRecyclerView;
        ///////////////bookmarks
        TextView bookmarks, containsInternalStorage,hide;
        RecyclerView bookmarkRecycleView;
        ///////////////internal storage
        TextView availableStorage,useOf,progressValue;
        ProgressBar progressBar;
        ConstraintLayout cl;
        //////////////common recycle view and text view
        TextView noFileAvailable;
        RecyclerView recyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //////////////////pie chat
            pieChart = itemView.findViewById(R.id.pie_chart);
            pieRecyclerView = itemView.findViewById(R.id.rec_pie_chart);
            internal = itemView.findViewById(R.id.internal);
            ////////////////////bookmark
            bookmarks = itemView.findViewById(R.id.bookmark_text_view);
            containsInternalStorage = itemView.findViewById(R.id.contains_internal_storage_text_view);
            hide = itemView.findViewById(R.id.hide_text_view);
            bookmarkRecycleView = itemView.findViewById(R.id.rec_bookmarks);
            ///////////////////internal storage
            availableStorage = itemView.findViewById(R.id.available_text_view);
            useOf = itemView.findViewById(R.id.use_of_text_view);
            progressValue = itemView.findViewById(R.id.progress_text_view);
            progressBar = itemView.findViewById(R.id.progressBar);
            cl =  itemView.findViewById(R.id.card_constraint1);
            ///////////////////common recycle view and text view
            noFileAvailable = itemView.findViewById(R.id.no_file_available);
            recyclerView= itemView.findViewById(R.id.rec);

        }


    }

}
