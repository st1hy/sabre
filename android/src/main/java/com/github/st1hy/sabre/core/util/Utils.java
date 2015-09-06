/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.st1hy.sabre.core.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;

/**
 * Taken from DisplayingBitmaps example.
 *
 ******************************************************************************
 * Class containing some static utility methods.
 */
public enum Utils {
    ;

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M;
    }

    public static boolean hasLolipop() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    public static int getColor(Context context, int colorResId) {
        Resources resources = context.getResources();
        if (hasMarshmallow()) {
            return getColorM(resources, context.getTheme(), colorResId);
        } else {
            return getColorDeprecated(resources, colorResId);
        }
    }

    @TargetApi(23)
    private static int getColorM(Resources resources, Resources.Theme theme, int colorResId) {
        return resources.getColor(colorResId, theme);
    }

    private static int getColorDeprecated(Resources resources, int colorResId) {
        return resources.getColor(colorResId);
    }

    public static Drawable getDrawable(Context context, int drawableResId) {
        Resources resources = context.getResources();
        int density = resources.getDisplayMetrics().densityDpi;
        if (hasLolipop()) {
            return getDrawableL(resources, context.getTheme(), drawableResId, density);
        } else {
            return getDrawableDeprecated(resources, drawableResId, density);
        }
    }

    @TargetApi(21)
    private static Drawable getDrawableL(Resources resources, Resources.Theme theme, int colorResId, int density) {
        return resources.getDrawableForDensity(colorResId, density, theme);
    }

    private static Drawable getDrawableDeprecated(Resources resources, int colorResId, int density) {
        return resources.getDrawableForDensity(colorResId, density);
    }
}
