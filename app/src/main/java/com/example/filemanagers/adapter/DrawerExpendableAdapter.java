package com.example.filemanagers.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanagers.R;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IExpandable;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.ISubItem;
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.materialize.holder.StringHolder;

import java.util.List;

public class  DrawerExpendableAdapter <Parent extends IItem & IExpandable, SubItem extends IItem & ISubItem> extends AbstractExpandableItem<DrawerExpendableAdapter<Parent, SubItem>, DrawerExpendableAdapter.ViewHolder, SubItem> {

    public DrawerExpendableAdapter(StringHolder name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public StringHolder name;
    public int icon;

    private OnClickListener<DrawerExpendableAdapter> mOnClickListener;


    public DrawerExpendableAdapter<Parent, SubItem> withName(String Name) {
        this.name = new StringHolder(Name);
        return this;
    }

    public DrawerExpendableAdapter<Parent, SubItem> withName(@StringRes int NameRes) {
        this.name = new StringHolder(NameRes);
        return this;
    }

    public OnClickListener<DrawerExpendableAdapter> getOnClickListener() {
        return mOnClickListener;
    }

    public DrawerExpendableAdapter<Parent, SubItem> withOnClickListener(OnClickListener<DrawerExpendableAdapter> mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
        return this;
    }

    //we define a clickListener in here so we can directly animate
    final private OnClickListener<DrawerExpendableAdapter<Parent, SubItem>> onClickListener = new OnClickListener<DrawerExpendableAdapter<Parent, SubItem>>() {
        @Override
        public boolean onClick(View v, IAdapter adapter, @NonNull DrawerExpendableAdapter item, int position) {
            if (item.getSubItems() != null) {
                if (!item.isExpanded()) {
                    ViewCompat.animate(v.findViewById(R.id.drawable_expendable_icon)).rotation(180).start();
                } else {
                    ViewCompat.animate(v.findViewById(R.id.drawable_expendable_icon)).rotation(0).start();
                }
                return mOnClickListener == null || mOnClickListener.onClick(v, adapter, item, position);
            }
            return mOnClickListener != null && mOnClickListener.onClick(v, adapter, item, position);
        }
    };

    @Override
    public OnClickListener<DrawerExpendableAdapter<Parent, SubItem>> getOnItemClickListener() {
        return onClickListener;
    }

    @Override
    public boolean isSelectable() {
        //this might not be true for your application
        return getSubItems() == null;
    }

    @Override
    public int getType() {
        return R.id.drawable_bottom_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.each_row_of_drawable_bottom_layout;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        Context ctx = viewHolder.itemView.getContext();

        viewHolder.itemView.clearAnimation();
        ViewCompat.setBackground(viewHolder.itemView, FastAdapterUIUtils.getSelectableBackground(ctx, ctx.getColor(R.color.gray1), true));
        StringHolder.applyTo(name, viewHolder.name);
        viewHolder.icon.setImageResource(icon);

        if (getSubItems() == null || getSubItems().size() == 0) {
            viewHolder.expendableIcon.setVisibility(View.GONE);
        } else {
            viewHolder.expendableIcon.setVisibility(View.VISIBLE);
        }

        if (isExpanded()) {
            ViewCompat.setRotation(viewHolder.expendableIcon, 0);
        } else {
            ViewCompat.setRotation(viewHolder.expendableIcon, 180);
        }
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.name.setText(null);
        holder.icon.clearAnimation();
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView icon, expendableIcon;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.drawable_name);
            icon = view.findViewById(R.id.drawable_icon);
            expendableIcon = view.findViewById(R.id.drawable_expendable_icon);
        }
    }
}

