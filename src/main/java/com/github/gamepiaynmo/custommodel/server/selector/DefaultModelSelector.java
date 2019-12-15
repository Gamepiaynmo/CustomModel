package com.github.gamepiaynmo.custommodel.server.selector;

import com.github.gamepiaynmo.custommodel.api.IModelSelector;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

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
    public Collection<String> getModelForPlayer(MinecraftServer server, ServerPlayerEntity playerEntity) {
        GameProfile profile = playerEntity.getGameProfile();
        if (models.contains(profile))
            return ImmutableList.of(models.get(profile).getModelPack());

        String nameEntry = profile.getName().toLowerCase();
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        String uuidEntry = uuid.toString().toLowerCase();
        return ImmutableList.of(nameEntry, uuidEntry, nameEntry + ".zip", uuidEntry + ".zip");
    }

    @Override
    public void onModelSelected(MinecraftServer server, ServerPlayerEntity playerEntity, String model) {
        models.add(new ModelEntry(playerEntity.getGameProfile(), model));
        saveModelList();
    }
}
