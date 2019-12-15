package com.github.gamepiaynmo.custommodel.server.selector;

import com.github.gamepiaynmo.custommodel.server.selector.ModelEntry;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import java.io.File;

public class ModelList extends ServerConfigList<GameProfile, ModelEntry> {
    public ModelList(File file) {
        super(file);
    }

    @Override
    protected ServerConfigEntry<GameProfile> fromJson(JsonObject jsonObject) {
        return new ModelEntry(jsonObject);
    }

    @Override
    protected String toString(GameProfile gameProfile) {
        return gameProfile.getId().toString();
    }

    @Override
    public String[] getNames() {
        String[] strings = new String[this.values().size()];

        int i = 0;
        for (ServerConfigEntry<GameProfile> entry : values()) {
            strings[i++] = entry.getKey().getName();
        }

        return strings;
    }
}
