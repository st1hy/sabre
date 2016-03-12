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

package com.github.st1hy.imagecache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.imagecache.decoder.BitmapDecoder;
import com.github.st1hy.imagecache.resize.InputDownSampling;
import com.github.st1hy.imagecache.resize.ResizingStrategy;
import com.github.st1hy.imagecache.reuse.RefHandle;
import com.github.st1hy.imagecache.reuse.ReusableBitmapPool;

import timber.log.Timber;

public class BitmapProvider<Source> {
    private int requiredWidth;
    private int requiredHeight;
    private ReusableBitmapPool reusableBitmapPool;
    private BitmapDecoder<Source> bitmapDecoder;
    private ResizingStrategy resizingStrategy;

    private BitmapProvider() {
    }

    @Nullable
    public RefHandle<Bitmap> getImage(@NonNull Source source, @Nullable Rect outPadding, @Nullable BitmapFactory.Options options) {
        if (options == null) options = new BitmapFactory.Options();
        final boolean isDecodingBounds = resizingStrategy.isInSampleSizeUsed() || resizingStrategy.isReusingBitmaps();
        if (isDecodingBounds) {
            options.inJustDecodeBounds = true;
            bitmapDecoder.decode(source, outPadding, options);
            if (resizingStrategy.isInSampleSizeUsed()) {
                options.inSampleSize = resizingStrategy.calculateInSampleSize(options, requiredWidth, requiredHeight);
            } else {
                options.inSampleSize = 1;
            }
        }
        if (resizingStrategy.isReusingBitmaps()) {
            findBitmapToReuse(options);
        }
        Bitmap bitmap = null;
        boolean isErrorTryingToDecode = isDecodingBounds && options.outHeight == -1;
        if (!isErrorTryingToDecode) {
            options.inJustDecodeBounds = false;
            bitmap = bitmapDecoder.decode(source, outPadding, options);
            if (resizingStrategy.isResizingRequired() && bitmap != null) {
                bitmap = resizingStrategy.resizeBitmap(bitmap, requiredWidth, requiredHeight);
            }
        }
        if (bitmap == null) {
            Timber.e("Error trying to decode image");
            return null;
        }
        if (reusableBitmapPool != null) {
            return reusableBitmapPool.newBitmapHandleForReuse(bitmap);
        } else {
            return RefHandle.newHandle(bitmap);
        }
    }

    @Nullable
    public RefHandle<Bitmap> getImage(@NonNull Source source, @Nullable Rect outPadding) {
        return getImage(source, outPadding, null);
    }

    @Nullable
    public RefHandle<Bitmap> getImage(@NonNull Source source) {
        return getImage(source, null, null);
    }

    private void findBitmapToReuse(@NonNull BitmapFactory.Options options) {
        if (reusableBitmapPool == null) return;
        options.inMutable = true;
        Bitmap bitmap = reusableBitmapPool.getBitmapFromReusableSet(options);
        if (bitmap != null) {
            options.inBitmap = bitmap;
        }
    }

    public static class Builder<Source> {
        private int requiredWidth = Integer.MAX_VALUE;
        private int requiredHeight = Integer.MAX_VALUE;
        private ReusableBitmapPool reusableBitmapPool;
        private BitmapDecoder<Source> bitmapDecoder;
        private ResizingStrategy resizingStrategy;

        public Builder(@NonNull BitmapDecoder<Source> bitmapDecoder) {
            this.bitmapDecoder = bitmapDecoder;
        }

        public Builder setRequiredSize(int requiredWidth, int requiredHeight) {
            this.requiredWidth = requiredWidth;
            this.requiredHeight = requiredHeight;
            return this;
        }

        public Builder setReusableBitmapPool(@Nullable ReusableBitmapPool reusableBitmapPool) {
            this.reusableBitmapPool = reusableBitmapPool;
            return this;
        }

        public Builder setResizingStrategy(@NonNull ResizingStrategy resizingStrategy) {
            this.resizingStrategy = resizingStrategy;
            return this;
        }

        public BitmapProvider<Source> build() {
            BitmapProvider<Source> bitmapProvider = new BitmapProvider<>();
            bitmapProvider.bitmapDecoder = bitmapDecoder;
            bitmapProvider.reusableBitmapPool = reusableBitmapPool;
            bitmapProvider.requiredHeight = requiredHeight;
            bitmapProvider.requiredWidth = requiredWidth;
            if (resizingStrategy == null) resizingStrategy = new InputDownSampling();
            bitmapProvider.resizingStrategy = resizingStrategy;
            return bitmapProvider;
        }
    }
}
