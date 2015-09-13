package com.github.st1hy.sabre.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.st1hy.sabre.R;

public class HistoryEntryHolder extends RecyclerView.ViewHolder {
    private final boolean empty;
    private final ImageReceiverView image;
    private final TextView imageName;
    private final TextView lastAccess;

    private final View materialRippleLayout;

    private final View root;

    private HistoryEntryHolder(View itemView) {
        this(itemView, false);
    }

    private HistoryEntryHolder(View itemView, boolean isEmpty) {
        super(itemView);
        this.root = itemView;
        empty = isEmpty;
        if (!isEmpty) {
            materialRippleLayout = itemView.findViewById(R.id.history_material_frame);
            image = (ImageReceiverView) itemView.findViewById(R.id.entry_image);
            imageName = (TextView) itemView.findViewById(R.id.entry_resource);
            lastAccess = (TextView) itemView.findViewById(R.id.entry_date);
        } else {
            materialRippleLayout = null;
            image = null;
            imageName = null;
            lastAccess = null;
        }
    }

    public static HistoryEntryHolder newHistoryItem(View itemView) {
        return new HistoryEntryHolder(itemView);
    }

    public static HistoryEntryHolder newEmptyItem(View itemView) {
        return new HistoryEntryHolder(itemView, true);
    }

    public View getRoot() {
        return root;
    }

    public TextView getLastAccess() {
        return lastAccess;
    }

    public TextView getImageName() {
        return imageName;
    }

    public ImageReceiverView getImage() {
        return image;
    }

    public boolean isEmpty() {
        return empty;
    }

    public View getMaterialRippleLayout() {
        return materialRippleLayout;
    }
}
