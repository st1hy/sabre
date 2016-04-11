package rx.picasso;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.squareup.picasso.Picasso;

public final class BitmapLoadedEvent implements PicassoEvent {
    private final Bitmap bitmap;
    private final Picasso.LoadedFrom from;

    public BitmapLoadedEvent(@NonNull  Bitmap bitmap, @NonNull Picasso.LoadedFrom from) {
        this.bitmap = bitmap;
        this.from = from;
    }

    @NonNull
    public Bitmap getBitmap() {
        return bitmap;
    }

    @NonNull
    public Picasso.LoadedFrom getFrom() {
        return from;
    }
}
