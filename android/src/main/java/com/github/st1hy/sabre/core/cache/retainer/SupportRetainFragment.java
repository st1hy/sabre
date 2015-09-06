package com.github.st1hy.sabre.core.cache.retainer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * A simple non-UI Fragment that stores a single Object and is retained over configuration
 * changes. It will be used to retain the ImageCache object.
 */
public class SupportRetainFragment extends Fragment implements Retainer {
    private static final String TAG = "SupportRetainFragment";
    private final Retainer retainer = new ObjectRetainer();

    /**
     * Empty constructor as per the Fragment documentation
     */
    public SupportRetainFragment() {
    }

    @Override
    public Object get(String key) {
        return retainer.get(key);
    }

    @Override
    public void put(String key, Object value) {
        retainer.put(key, value);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
    public static SupportRetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        // Check to see if we have retained the worker fragment.
        SupportRetainFragment mRetainFragment = (SupportRetainFragment) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add it.
        if (mRetainFragment == null) {
            mRetainFragment = new SupportRetainFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
        }

        return mRetainFragment;
    }
}
