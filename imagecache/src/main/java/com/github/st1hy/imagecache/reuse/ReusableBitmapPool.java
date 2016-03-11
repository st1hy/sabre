package com.github.st1hy.imagecache.reuse;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.core.utils.Utils;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import timber.log.Timber;

public class ReusableBitmapPool {
    // If we're running on Honeycomb or newer, create a set of reusable bitmaps that can be
    // populated into the inBitmap field of BitmapFactory.Options. Note that the set is
    // of SoftReferences which will actually not be very effective due to the garbage
    // collector being aggressive clearing Soft/WeakReferences. A better approach
    // would be to use a strongly references bitmaps, however this would require some
    // balancing of memory usage between this set and the bitmap LruCache. It would also
    // require knowledge of the expected size of the bitmaps. From Honeycomb to JellyBean
    // the size would need to be precise, from KitKat onward the size would just need to
    // be the upper bound (due to changes in how inBitmap can re-use bitmaps).
    private final Set<SoftReference<Bitmap>> mReusableBitmaps;

    public ReusableBitmapPool() {
        mReusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
    }

    /**
     * @param options - BitmapFactory.Options with out* options populated
     * @return Bitmap that case be used for inBitmap
     */
    @Nullable
    public Bitmap getBitmapFromReusableSet(@NonNull BitmapFactory.Options options) {
        if (mReusableBitmaps.isEmpty()) return null;
        Bitmap bitmap = null;
        synchronized (mReusableBitmaps) {
            Timber.d("Searching for reusable bitmap.");
            final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
            Bitmap item;

            while (iterator.hasNext()) {
                item = iterator.next().get();

                if (null != item && item.isMutable()) {
                    // Check to see it the item can be used for inBitmap
                    if (canUseForInBitmap(item, options)) {
                        bitmap = item;

                        // Remove from reusable set so it can't be used again
                        iterator.remove();
                        break;
                    }
                } else {
                    // Remove from the set if the reference has been cleared.
                    iterator.remove();
                }
            }
            Timber.d("Search complete. Found: " + (bitmap != null ? bitmap.hashCode() : "null"));
        }

        return bitmap;
    }


    /**
     * @param candidate     - Bitmap to check
     * @param targetOptions - Options that have the out* value populated
     * @return true if <code>candidate</code> can be used for inBitmap re-use with
     * <code>targetOptions</code>
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean canUseForInBitmap(
            @NonNull Bitmap candidate, @NonNull BitmapFactory.Options targetOptions) {
        if (!Utils.hasKitKat()) {
            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
            return candidate.getWidth() == targetOptions.outWidth
                    && candidate.getHeight() == targetOptions.outHeight
                    && targetOptions.inSampleSize == 1;
        } else {

            // From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
            // is smaller than the reusable bitmap candidate allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }
    }

    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     *
     * @param config The bitmap configuration.
     * @return The byte usage per pixel.
     */
    private static int getBytesPerPixel(@NonNull Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }
}
