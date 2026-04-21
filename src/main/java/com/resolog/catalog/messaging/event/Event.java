package com.resolog.catalog.messaging.event;

public interface Event {
    default String eventName() {
        return  this.getClass().getSimpleName();
    }
}
