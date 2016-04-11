package com.github.st1hy.sabre.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.st1hy.sabre.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryEntryHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.entry_image)
    ImageView image;
    @Bind(R.id.entry_resource)
    TextView imageName;
    @Bind(R.id.entry_date)
    TextView lastAccess;
    @Bind(R.id.history_material_frame)
    View materialRippleLayout;

    private HistoryEntryHolder(View itemView) {
        super(itemView);
    }

    public static HistoryEntryHolder newHistoryItem(View itemView) {
        HistoryEntryHolder holder = new HistoryEntryHolder(itemView);
        ButterKnife.bind(holder, itemView);
        return holder;
    }

    public TextView getLastAccess() {
        return lastAccess;
    }

    public TextView getImageName() {
        return imageName;
    }

    public ImageView getImage() {
        return image;
    }

    public View getMaterialRippleLayout() {
        return materialRippleLayout;
    }
}
