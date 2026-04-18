package com.resolog.catalog.domain.model;

public final class DbSchema {

    private DbSchema() {}

    public static final class Artists {
        public static final String TABLE = "artists";
        public static final String ID = "artist_id";
        private Artists() {}
    }

    public static final class Products {
        public static final String TABLE = "products";
        public static final String ID = "product_id";
        public static final String PRODUCT_ARTIST_TABLE = "product_artist";
        public static final String RELEASE_DATE = "release_date";
        public static final String ARTWORK_URL = "artwork_url";
        public static final String ARTIST_ID = "artist_id";
        public static final String STATUS_REASON = "status_reason";
        private Products() {}
    }

    public static final class Tracks {
        public static final String TABLE = "tracks";
        public static final String ID = "track_id";
        public static final String TRACK_ARTIST_TABLE = "track_artist";
        public static final String TRACK_NUMBER = "track_number";
        public static final String DURATION_SECONDS = "duration_seconds";
        public static final String AUDIO_URL = "audio_url";
        public static final String PRODUCT_ID = "product_id";
        public static final String ARTIST_ID = "artist_id";
        private Tracks() {}
    }

    public static final class OutboxEvents {
        public static final String TABLE = "outbox_events";
        public static final String ID = "outbox_event_id";
        public static final String AGGREGATE_ID = "aggregate_id";
        public static final String EVENT_TYPE = "event_type";
        public static final String SENT_AT = "sent_at";
        private OutboxEvents() {}
    }
}
