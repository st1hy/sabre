package com.github.st1hy.core.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class MissingInterfaceException extends IllegalStateException {

    public MissingInterfaceException(@NonNull Class mClass) {
        super("Parent must implement: "+ mClass.getSimpleName());
    }

    public static void parentSanityCheck(@NonNull Fragment fragment, @NonNull Class mClass) throws MissingInterfaceException{
        if (!(mClass.isInstance(fragment.getActivity())))
            throw new MissingInterfaceException(mClass);
    }
}
