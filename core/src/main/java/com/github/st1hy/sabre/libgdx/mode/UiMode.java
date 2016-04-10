package com.github.st1hy.sabre.libgdx.mode;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public enum UiMode {
    MOVE_CAMERA, MOVE_ELEMENT, CUT_ELEMENT;

    public static final UiMode DEFAULT = MOVE_CAMERA;
    public static UiMode GLOBAL = DEFAULT;
    private static Subject<UiMode, UiMode> BUS = new SerializedSubject<>(PublishSubject.<UiMode>create());

    public static void setGlobalMode(final UiMode mode) {
        if (GLOBAL == mode) return;
        GLOBAL = mode;
        BUS.onNext(mode);
    }

    public static Observable<UiMode> toObservable() {
        return BUS;
    }
}
