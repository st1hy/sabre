package com.github.st1hy.sabre.core.util;

import android.support.v4.app.Fragment;

import com.github.st1hy.sabre.core.cache.CacheProvider;

public class MissingInterfaceException extends IllegalStateException {

    public MissingInterfaceException(Class mClass) {
        super("Parent must implement: "+ mClass.getSimpleName());
    }

    public static void parentSanityCheck(Fragment fragment, Class mClass) throws MissingInterfaceException{
        if (!(mClass.isInstance(fragment.getActivity())))
            throw new MissingInterfaceException(CacheProvider.class);
    }
}
