package com.github.st1hy.sabre.core.injector;

import android.support.annotation.NonNull;
import android.view.View;

import butterknife.ButterKnife;

public abstract class ViewBinder {
    private View root;

    public ViewBinder bind(@NonNull View view) {
        root = view;
        ButterKnife.bind(this, view);
        return this;
    }

    public View getRoot() {
        return root;
    }
}
