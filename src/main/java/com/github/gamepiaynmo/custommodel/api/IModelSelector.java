package com.github.gamepiaynmo.custommodel.api;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public interface IModelSelector {

    Collection<String> getModelForPlayer(MinecraftServer server, ServerPlayerEntity playerEntity);

    void onModelSelected(MinecraftServer server, ServerPlayerEntity playerEntity, String model);

    public static void setModelSelector(IModelSelector modelSelector) {
        CustomModel.setModelSelector(modelSelector);
    }
}
