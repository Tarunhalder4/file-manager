package com.example.filemanagers.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagers.Constant;
import com.example.filemanagers.R;
import com.mikepenz.fastadapter.IClickable;
import com.mikepenz.fastadapter.IExpandable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.ISubItem;
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter_extensions.drag.IDraggable;
import com.mikepenz.materialize.holder.StringHolder;

import java.util.List;

public class DrawableItemAdapter<Parent extends IItem & IExpandable & ISubItem & IClickable> extends AbstractExpandableItem<Parent, DrawableItemAdapter.ViewHolder, DrawableItemAdapter<Parent>> implements IDraggable<DrawableItemAdapter, IItem> {

    public DrawableItemAdapter(StringHolder name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public StringHolder name;
    public int icon;

    private boolean mIsDraggable = true;


    public DrawableItemAdapter<Parent> withName(String Name) {
        this.name = new StringHolder(Name);
        return this;
    }

    public DrawableItemAdapter<Parent> withName(@StringRes int NameRes) {
        this.name = new StringHolder(NameRes);
        return this;
    }

    @Override
    public boolean isDraggable() {
        return mIsDraggable;
    }

    @Override
    public DrawableItemAdapter withIsDraggable(boolean draggable) {
        this.mIsDraggable = draggable;
        return this;
    }

    @Override
    public int getType() {
        return R.id.simple_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.simple_item;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        Context ctx = viewHolder.itemView.getContext();
        viewHolder.itemView.clearAnimation();
        ViewCompat.setBackground(viewHolder.itemView, FastAdapterUIUtils.getSelectableBackground(ctx, Color.GRAY, true));
        StringHolder.applyTo(name, viewHolder.name);
        viewHolder.icon.setImageResource(icon);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.name.setText(null);
        holder.icon.setImageResource(0);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.drawable_item_name);
            icon = view.findViewById(R.id.drawable_item_icon);
        }
    }
}
