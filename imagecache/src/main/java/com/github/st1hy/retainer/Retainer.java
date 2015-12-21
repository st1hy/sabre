package com.github.st1hy.retainer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Retainer {
    @Nullable
    Object get(@NonNull String key);

    void put(@NonNull String key, @Nullable Object value);
}
