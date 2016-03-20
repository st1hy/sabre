package com.github.st1hy.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TextureManager extends ApplicationAdapter {
    private Map<String, Texture> textureMap = new HashMap<String, Texture>();

    public void put(String key, Texture tex) {
        Texture texture = get(key);
        if (texture != null) texture.dispose();
        textureMap.put(key, tex);
    }

    public void remove(String key) {
        Texture texture = get(key);
        if (texture != null) texture.dispose();
        textureMap.remove(key);
    }

    public Texture get(String key) {
        return textureMap.get(key);
    }

    @Override
    public void dispose() {
        Collection<Texture> values = textureMap.values();
        for (Texture tex : values) {
            if (tex != null) tex.dispose();
        }
        textureMap.clear();
    }
}
