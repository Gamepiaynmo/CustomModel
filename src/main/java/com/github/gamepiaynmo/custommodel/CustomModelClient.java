package com.github.gamepiaynmo.custommodel;

import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.CustomPlayerEntityRenderer;
import com.github.gamepiaynmo.custommodel.util.ModelPack;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class CustomModelClient implements ClientModInitializer {
    private static Map<String, ModelPack> modelPacks = Maps.newHashMap();

    public static TextureManager textureManager;
    public static CustomPlayerEntityRenderer customRenderer;

    public static void reloadModels() {
        modelPacks.clear();
        File modelFolder = new File("custom-models");
        if (modelFolder.isDirectory()) {
            for (File modelPackFile : modelFolder.listFiles()) {
                try {
                    if (modelPackFile.isDirectory()) {
                        ModelPack pack = ModelPack.fromDirectory(textureManager, modelPackFile);
                        if (pack.successfulLoaded())
                            modelPacks.put(modelPackFile.getName(), pack);
                    } else if (modelPackFile.getName().endsWith(".zip")) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ModelPack getModelForPlayer(AbstractClientPlayerEntity playerEntity) {
        ModelPack pack = modelPacks.get(playerEntity.getGameProfile().getName());
        if (pack == null)
            pack = modelPacks.get(PlayerEntity.getUuidFromProfile(playerEntity.getGameProfile()));
        return pack;
    }

    public static Collection<String> getAvailableModelPacks() {
        return modelPacks.keySet();
    }

    public static ModelPack getModelForName(String name) {
        return modelPacks.get(name);
    }

    @Override
    public void onInitializeClient() {
        WorldTickCallback.EVENT.register(world -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (world == client.world) {
                for (AbstractClientPlayerEntity player : client.world.getPlayers())
                    customRenderer.tick(player);
            }
        });
    }
}
