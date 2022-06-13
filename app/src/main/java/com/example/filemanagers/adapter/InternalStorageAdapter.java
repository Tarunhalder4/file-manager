package com.example.filemanagers.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagers.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class InternalStorageAdapter extends AbstractItem<InternalStorageAdapter, InternalStorageAdapter.ViewHolder> {

    String availableStorage;
    String useOfValue;
    String totalValue;
    String progressValue;

    public InternalStorageAdapter(String availableStorage, String useOfValue, String totalValue, String progressValue) {
        this.availableStorage = availableStorage;
        this.useOfValue = useOfValue;
        this.totalValue = totalValue;
        this.progressValue = progressValue;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.internal_storage_card_view_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.internal_storage_cardview;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.availableStorage.setText("Available "+availableStorage+" GB");
        holder.useOf.setText(useOfValue+" GB Used of "+totalValue+" GB");
        holder.progressValue.setText(progressValue);
        holder.progressBar.setProgress(Integer.parseInt(progressValue));
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.availableStorage.setText(null);
        holder.useOf.setText(null);
        holder.progressValue.setText(null);
        holder.progressBar.setProgress(0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView availableStorage,useOf,progressValue;
        ProgressBar progressBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            availableStorage = itemView.findViewById(R.id.available_text_view);
            useOf = itemView.findViewById(R.id.use_of_text_view);
            progressValue = itemView.findViewById(R.id.progress_text_view);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
