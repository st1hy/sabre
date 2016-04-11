package rx.picasso;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public final class BitmapFailedEvent implements PicassoEvent {
    private final Drawable errorDrawable;

    public BitmapFailedEvent(@Nullable  Drawable errorDrawable) {
        this.errorDrawable = errorDrawable;
    }

    @Nullable
    public Drawable getErrorDrawable() {
        return errorDrawable;
    }
}
