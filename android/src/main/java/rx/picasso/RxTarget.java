package rx.picasso;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public final class RxTarget implements Target {
    private final Subject<TargetEvent, TargetEvent> subject = new SerializedSubject<>(PublishSubject.<TargetEvent>create());

    private RxTarget() {
    }

    @NonNull
    public static RxTarget get() {
        return new RxTarget();
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        subject.onNext(TargetEvent.from(new BitmapLoadedEvent(bitmap, from)));
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        subject.onNext(TargetEvent.from(new BitmapFailedEvent(errorDrawable)));
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        subject.onNext(TargetEvent.from(new PrepareLoadEvent(placeHolderDrawable)));
    }

    public Observable<TargetEvent> toObservable() {
        return subject;
    }
}
