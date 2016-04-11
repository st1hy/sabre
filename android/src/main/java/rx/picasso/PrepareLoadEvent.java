package rx.picasso;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public final class PrepareLoadEvent implements PicassoEvent {
    private final Drawable placeHolderDrawable;

    public PrepareLoadEvent(@Nullable  Drawable placeHolderDrawable) {
        this.placeHolderDrawable = placeHolderDrawable;
    }

    @Nullable
    public Drawable getPlaceHolderDrawable() {
        return placeHolderDrawable;
    }
}
