package com.github.st1hy.retainer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A simple non-UI Fragment that stores a single Object and is retained over configuration
 * changes. It will be used to retain the ImageCache object.
 */
public class RetainFragment extends Fragment implements Retainer{
    private static final String TAG = "RetainFragment";
    private final Retainer retainer = new ObjectRetainer();

    /**
     * Empty constructor as per the Fragment documentation
     */
    public RetainFragment() {
    }

    @Override
    @Nullable
    public Object get(@NonNull String key) {
        return retainer.get(key);
    }

    @Override
    public void put(@NonNull String key, @Nullable Object value) {
        retainer.put(key, value);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure this Fragment is retained over a configuration change
        setRetainInstance(true);
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     *
     * @param fm The FragmentManager manager to use.
     * @return The existing instance of the Fragment or the new instance if just
     * created.
     */
    public static Retainer findOrCreateRetainFragment(@NonNull FragmentManager fm) {
        // Check to see if we have retained the worker fragment.
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add it.
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
        }

        return mRetainFragment;
    }
}
