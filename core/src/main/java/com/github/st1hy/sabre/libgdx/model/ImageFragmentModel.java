package com.github.st1hy.sabre.libgdx.model;

import com.github.st1hy.coregdx.Transformation;

import java.io.Serializable;

public class ImageFragmentModel implements Serializable {
    private float[] vertices;
    private Transformation imageTransformation;

    public ImageFragmentModel() {
        imageTransformation = new Transformation();
    }

    public ImageFragmentModel(float[] vertices) {
        this();
        this.vertices = vertices;
    }

    public float[] getVertices() {
        return vertices;
    }

    public Transformation getImageTransformation() {
        return imageTransformation;
    }
}
