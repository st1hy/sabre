package com.github.st1hy.sabre.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.github.st1hy.coregdx.Transformation;
import com.github.st1hy.sabre.libgdx.fragments.Elevation;
import com.github.st1hy.sabre.libgdx.model.ImageFragmentModel;
import com.github.st1hy.sabre.libgdx.model.ScreenModel;

public class ScreenContext {
    private final Texture background;
    private final ScreenModel screenModel;

    private final Transformation screenTransformation = new Transformation();
    private final Transformation worldTransformation = new Transformation();
    private final Elevation elevation = new Elevation(Gdx.graphics.getDensity());

    private ScreenContext(Texture background, ScreenModel screenModel) {
        this.background = background;
        this.screenModel = screenModel;
    }

    public Texture getBackground() {
        return background;
    }

    public Array<ImageFragmentModel> getFragmentModels() {
        return screenModel.getFragmentModels();
    }

    /**
     *
     * @return user transformation of the screen (rotation, translation and scale combined) from user touch input
     */
    public Transformation getScreenTransformation() {
        return screenTransformation;
    }

    /**
     * @return transformation from image coordinates to screen coordinates. It contains 2 parts:
     * 1. Initial transformation 'A' - transforms image to fit the screen (keeping aspect ratio) and
     * centers image
     * 2. User transformation 'B' - transformation of the screen created by user touch movement
     * Result transformation 'T' is defined as: T := BA
     */
    public Transformation getWorldTransformation() {
        return worldTransformation;
    }

    public void resetTransformations() {
        screenTransformation.idt();
        worldTransformation.idt();
    }

    public Elevation getElevation() {
        return elevation;
    }

    public void dispose() {
        background.dispose();
    }

    public static ScreenContext newScreenContext(Texture background) {
        ScreenModel screenModel = new ScreenModel();
        screenModel.setFragmentModels(new Array<ImageFragmentModel>());
        return new ScreenContext(background, screenModel);
    }

    public static ScreenContext createScreenContext(Texture background, String jsonString) {
        if (jsonString == null) return newScreenContext(background);
        Json json = new Json();
        ScreenModel screenModel = json.fromJson(ScreenModel.class, jsonString);
        return new ScreenContext(background, screenModel);
    }

    public String toJson() {
        Json json = new Json();
        return json.toJson(screenModel);
    }
}
