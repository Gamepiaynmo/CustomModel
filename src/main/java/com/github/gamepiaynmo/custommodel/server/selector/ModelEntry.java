package com.github.gamepiaynmo.custommodel.server.selector;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class ModelEntry extends ServerConfigEntry<GameProfile> {
    private final String modelPack;

    public ModelEntry(GameProfile gameProfile, String modelPack) {
        super(gameProfile);
        this.modelPack = modelPack;
    }

    public ModelEntry(JsonObject jsonObject) {
        super(getProfileFromJson(jsonObject), jsonObject);
        this.modelPack = jsonObject.has("model") ? jsonObject.get("model").getAsString() : "";
    }

    public String getModelPack() { return modelPack; }

    @Override
    protected void serialize(JsonObject jsonObject) {
        if (this.getKey() != null) {
            jsonObject.addProperty("uuid", this.getKey().getId() == null ? "" : this.getKey().getId().toString());
            jsonObject.addProperty("name", this.getKey().getName());
            super.serialize(jsonObject);
            jsonObject.addProperty("model", this.modelPack);
        }
    }

    private static GameProfile getProfileFromJson(JsonObject jsonObject_1) {
        if (jsonObject_1.has("uuid") && jsonObject_1.has("name")) {
            String string_1 = jsonObject_1.get("uuid").getAsString();

            UUID uUID_2;
            try {
                uUID_2 = UUID.fromString(string_1);
            } catch (Throwable var4) {
                return null;
            }

            return new GameProfile(uUID_2, jsonObject_1.get("name").getAsString());
        } else {
            return null;
        }
    }
}
