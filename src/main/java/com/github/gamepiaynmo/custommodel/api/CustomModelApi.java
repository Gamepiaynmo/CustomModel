package com.github.gamepiaynmo.custommodel.api;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public interface CustomModelApi {
    /**
     * Reload the model for the specified player.
     * @param player Specified player.
     */
    public static void reloadModelForPlayer(ServerPlayerEntity player) {
        CustomModel.reloadModel(player, true);
    }

    /**
     * Select a model for the specified player.
     * @param player Specified player.
     * @param modelId Model ID of the model pack.
     */
    public static void selectModelForPlayer(ServerPlayerEntity player, String modelId) {
        CustomModel.selectModel(player, modelId);
    }

    /**
     * Select a model for the specified offline player.
     * @param playerName Specified player name.
     * @param modelId Model ID of the model pack.
     */
    public static void selectModelForOfflinePlayer(String playerName, String modelId) {
        GameProfile profile = CustomModel.server.getUserCache().findByName(playerName);
        CustomModel.getModelSelector().setModelForPlayer(profile, modelId);
    }

    /**
     * Get the current model of the specified player.
     * @param player Specified player.
     * @return Model ID of the model pack.
     */
    public static String getCurrentModelOfPlayer(ServerPlayerEntity player) {
        return CustomModel.getModelSelector().getModelForPlayer(player.getGameProfile());
    }

    /**
     * Get the current model of the specified offline player.
     * @param playerName Specified player name.
     * @return Model ID of the model pack.
     */
    public static String getCurrentModelOfOfflinePlayer(String playerName) {
        GameProfile profile = CustomModel.server.getUserCache().findByName(playerName);
        return CustomModel.getModelSelector().getModelForPlayer(profile);
    }

    /**
     * Refresh the model list from disk files.
     */
    public static void refreshModelList() {
        CustomModel.refreshModelList();
    }

    /**
     * Get the mode ID of loaded model packs.
     * @return Model ID of loaded model packs.
     */
    public static Collection<String> getModelIdList() {
        return CustomModel.getModelIdList();
    }

    /**
     * Get the basic information of a loaded model pack.
     * @param modelId Model ID of the model pack.
     * @return Basic information of the model pack.
     */
    public static ModelPackInfo getModelPackInfo(String modelId) {
        ModelInfo model = CustomModel.models.get(modelId);
        return model == null ? null : model.getInfo();
    }
}
