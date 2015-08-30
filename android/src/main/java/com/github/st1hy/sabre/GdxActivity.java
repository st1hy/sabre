package com.github.st1hy.sabre;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.github.st1hy.core.SabreGDX;

public class GdxActivity extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
//        config.useImmersiveMode = true;
        initialize(new SabreGDX(), config);
    }
}
