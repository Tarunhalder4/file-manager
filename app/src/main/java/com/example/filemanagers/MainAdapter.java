package com.example.filemanagers;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class MainAdapter extends AbstractItem<MainAdapter, MainAdapter.ViewHolder> {

    FastItemAdapter<PieAdapter> pieAdapterFastItemAdapter;
    FastItemAdapter<BookmarkAdapter> bookmarkAdapterFastItemAdapter;
    private int layoutType;
    Context context;

    ///////////////////internal Storage
    String availableStorage;
    String useOfValue;
    String totalValue;
    String progressValue;

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
    public MainAdapter(String availableStorage, String useOfValue, String totalValue, String progressValue, int layoutType) {
        this.availableStorage = availableStorage;
        this.useOfValue = useOfValue;
        this.totalValue = totalValue;
        this.progressValue = progressValue;
        this.layoutType = layoutType;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        if (layoutType==Constant.PIE_CHAT){
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

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        if (layoutType==Constant.PIE_CHAT){
            holder.pieChat.setImageResource(R.drawable.file_48);
            holder.pieRecyclerView.setLayoutManager(new GridLayoutManager(context,2));
            holder.pieRecyclerView.setAdapter(pieAdapterFastItemAdapter);
        }

        if (layoutType==Constant.CONTAINS_INTERNAL_STORAGE||layoutType==Constant.BOOKMARK){
            if (layoutType==Constant.CONTAINS_INTERNAL_STORAGE){
                holder.bookmarks.setVisibility(View.INVISIBLE);
                holder.hide.setVisibility(View.INVISIBLE);
                holder.containsInternalStorage.setVisibility(View.VISIBLE);
                holder.bookmarkRecycleView.setLayoutManager(new GridLayoutManager(context,2));
                holder.bookmarkRecycleView.setAdapter(bookmarkAdapterFastItemAdapter);
            }else {
                holder.bookmarks.setVisibility(View.VISIBLE);
                holder.hide.setVisibility(View.VISIBLE);
                holder.containsInternalStorage.setVisibility(View.INVISIBLE);
                holder.bookmarkRecycleView.setLayoutManager(new GridLayoutManager(context,2));
                holder.bookmarkRecycleView.setAdapter(bookmarkAdapterFastItemAdapter);
            }
        }

        if (layoutType==Constant.INTERNAL_STORAGE){
            holder.availableStorage.setText("Available "+availableStorage+" GB");
            holder.useOf.setText(useOfValue+" GB Used of "+totalValue+" GB");
            holder.progressValue.setText(progressValue+"%");
            holder.progressBar.setProgress(Integer.parseInt(progressValue));
        }
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);

        if (layoutType==Constant.PIE_CHAT){
            holder.pieChat.setImageResource(0);
            holder.pieRecyclerView.setLayoutManager(new GridLayoutManager(context,3));
            holder.pieRecyclerView.setAdapter(null);
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
        ImageView pieChat;
        RecyclerView pieRecyclerView;
        ///////////////bookmarks
        TextView bookmarks, containsInternalStorage,hide;
        RecyclerView bookmarkRecycleView;
        ///////////////internal storage
        TextView availableStorage,useOf,progressValue;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //////////////////pie chat
            pieChat = itemView.findViewById(R.id.pie_image_view);
            pieRecyclerView = itemView.findViewById(R.id.rec_pie_chart);
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
        }


    }



}
