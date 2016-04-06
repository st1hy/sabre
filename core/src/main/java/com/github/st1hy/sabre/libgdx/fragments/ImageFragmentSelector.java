package com.github.st1hy.sabre.libgdx.fragments;

public interface ImageFragmentSelector {

    /**
     * x,y coordinates in view space (1 point = 1 px)
     */
    void onClickedOnImage(float screenX, float screenY);
}
