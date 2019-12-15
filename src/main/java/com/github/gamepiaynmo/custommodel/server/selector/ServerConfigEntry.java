package com.github.gamepiaynmo.custommodel.server.selector;

import com.google.gson.JsonObject;

public class ServerConfigEntry<T> {
    private final T object;

    public ServerConfigEntry(T object_1) {
        this.object = object_1;
    }

    protected ServerConfigEntry(T object_1, JsonObject jsonObject_1) {
        this.object = object_1;
    }

    T getKey() {
        return this.object;
    }

    boolean isInvalid() {
        return false;
    }

    protected void serialize(JsonObject jsonObject_1) {
    }
}
