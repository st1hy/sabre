package rx.picasso;

import android.support.annotation.NonNull;

public final class TargetEvent<T extends PicassoEvent> {
    private final T event;
    private final BitmapEventType type;

    public enum BitmapEventType {
        PREPARED(PrepareLoadEvent.class),
        FAILED(BitmapFailedEvent.class),
        LOADED(BitmapLoadedEvent.class);

        private final Class<? extends PicassoEvent> type;

        BitmapEventType(Class<? extends PicassoEvent> type) {
            this.type = type;
        }

        @NonNull
        public Class<? extends PicassoEvent> getType() {
            return type;
        }

        public static BitmapEventType from(@NonNull Class<? extends PicassoEvent> aClass) {
            for (BitmapEventType event : values()) {
                if (event.type.equals(aClass)) {
                    return event;
                }
            }
            throw new IllegalArgumentException("Cannot find corresponding event type");
        }
    }

    private TargetEvent(@NonNull T event, @NonNull BitmapEventType type) {
        this.event = event;
        this.type = type;
    }

    @NonNull
    public static <T extends PicassoEvent> TargetEvent<T> from(@NonNull T event) {
        return new TargetEvent<>(event, BitmapEventType.from(event.getClass()));
    }

    @NonNull
    public T getEvent() {
        return event;
    }

    @NonNull
    public BitmapEventType getType() {
        return type;
    }

}
