package com.aura.camerafilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.VH> {

    public interface OnFilterSelected {
        void onFilterSelected(int filterId);
    }

    private final List<FilterItem> items;
    private final OnFilterSelected listener;
    private Bitmap previewBitmap;
    private int selectedPos = 0;

    public FilterAdapter(List<FilterItem> items, OnFilterSelected listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setPreviewBitmap(Bitmap bmp) {
        this.previewBitmap = bmp;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FilterItem item = items.get(position);
        holder.label.setText(item.name);

        boolean selected = position == selectedPos;
        holder.label.setTextColor(selected ? 0xFFF5F0FF : 0xBBCCCCCC);
        holder.label.setAlpha(selected ? 1f : 0.72f);
        holder.ring.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);

        if (previewBitmap != null && !previewBitmap.isRecycled()) {
            Bitmap thumb = Bitmap.createScaledBitmap(previewBitmap, 80, 80, false);
            Bitmap filtered = FilterEngine.applyFilter(thumb, item.filterId);
            holder.thumbnail.setImageBitmap(filtered);
        } else {
            holder.thumbnail.setImageResource(android.R.color.darker_gray);
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPos;
            selectedPos = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPos);
            listener.onFilterSelected(item.filterId);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView  label;
        View      ring;

        VH(View v) {
            super(v);
            thumbnail = v.findViewById(R.id.iv_thumb);
            label     = v.findViewById(R.id.tv_label);
            ring      = v.findViewById(R.id.view_ring);
        }
    }
}
