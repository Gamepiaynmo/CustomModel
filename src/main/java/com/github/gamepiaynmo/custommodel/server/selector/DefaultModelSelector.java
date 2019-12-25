package com.github.gamepiaynmo.custommodel.server.selector;

import com.github.gamepiaynmo.custommodel.api.CustomModelApi;
import com.github.gamepiaynmo.custommodel.api.IModelSelector;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class DefaultModelSelector implements IModelSelector {
    private final ModelList models = new ModelList(new File("custom-models.json"));

    public DefaultModelSelector() {
        loadModelList();
        saveModelList();
    }

    private void loadModelList() {
        try {
            models.load();
        } catch (FileNotFoundException e) {
            CustomModel.LOGGER.warn("Failed to load custom model list: ", e);
        }
    }

    private void saveModelList() {
        try {
            models.save();
        } catch (IOException e) {
            CustomModel.LOGGER.warn("Failed to save custom model list: ", e);
        }
    }

    @Override
    public String getModelForPlayer(GameProfile profile) {
        if (models.contains(profile)) {
            String modelId = models.get(profile).getModelPack();
            if (CustomModelApi.getModelPackInfo(modelId) != null)
                return modelId;
        }

        return ModConfig.getDefaultModel();
    }

    @Override
    public void setModelForPlayer(GameProfile profile, String modelId) {
        models.add(new ModelEntry(profile, modelId));
        saveModelList();
    }

    @Override
    public void clearModelForPlayer(GameProfile profile) {
        models.remove(profile);
        saveModelList();
    }
}
