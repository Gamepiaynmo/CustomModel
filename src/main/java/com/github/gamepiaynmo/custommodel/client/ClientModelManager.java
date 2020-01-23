package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.server.ModelLoadInfo;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ClientModelManager {
    private final Map<UUID, ModelPack> modelPacks = Maps.newHashMap();
    private final Set<UUID> queried = Sets.newHashSet();
    private int clearCounter = 0;

    public void clearModels() {
        for (ModelPack pack : modelPacks.values())
            pack.release();
        modelPacks.clear();
        queried.clear();
    }

    public void clearModel(GameProfile profile) {
        clearModel(PlayerEntity.getUuidFromProfile(profile));
    }

    public void clearModel(UUID uuid) {
        PlayerEntity playerEntity = MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
        CustomModel.manager.getModelSelector().clearModelForPlayer(playerEntity.getGameProfile());
        releaseModel(uuid);
    }

    public void releaseModel(UUID uuid) {
        ModelPack old = modelPacks.remove(uuid);
        if (old != null)
            old.release();
    }

    public void addModel(UUID name, ModelPack pack) {
        ModelPack old = modelPacks.get(name);
        if (old != null)
            old.release();
        modelPacks.put(name, pack);
    }

    public boolean loadModel(UUID uuid, String model) {
        PlayerEntity playerEntity = MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
        ModelLoadInfo info = CustomModel.manager.models.get(model);
        ModelPack pack = null;

        try {
            if (info == null)
                throw new ModelNotFoundException(model);
            pack = ModelPack.fromModelInfo(CustomModelClient.textureManager, info.info, uuid);

            if (ModConfig.isSendModels() && CustomModelClient.isServerModded() && CustomModelClient.serverConfig.receiveModels) {
                PacketModel packetModel = info.info.getPacket(uuid);
                if (packetModel.success)
                    CustomModelClient.sendPacket(PacketModel.ID, packetModel);
            }
        } catch (ModelNotFoundException e) {
            CustomModelClient.LOGGER.warn(e.getMessage(), e);
        } catch (Exception e) {
            MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.CHAT,
                    new TranslatableText("error.custommodel.loadmodelpack", model, e.getMessage()).formatted(Formatting.RED));
            CustomModelClient.LOGGER.warn(e.getMessage(), e);
        }

        if (pack != null && pack.successfulLoaded()) {
            CustomModel.manager.getModelSelector().setModelForPlayer(playerEntity.getGameProfile(), model);
            addModel(uuid, pack);
            return true;
        }

        return false;
    }

    public void queryModel(GameProfile profile) {
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        queried.add(uuid);

        if (CustomModelClient.isServerModded())
            CustomModelClient.sendPacket(PacketQuery.ID, new PacketQuery(uuid));
        else reloadModel(profile);
    }

    public boolean selectModel(GameProfile profile, String model) {
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        return loadModel(uuid, model);
    }

    public boolean reloadModel(GameProfile profile) {
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        return loadModel(uuid, CustomModel.manager.getModelSelector().getModelForPlayer(profile));
    }

    public ModelPack getModelForPlayer(AbstractClientPlayerEntity player) {
        GameProfile profile = player.getGameProfile();
        UUID uuid = player.getUuid();

        ModelPack pack = modelPacks.get(uuid);
        if (pack != null)
            return pack;

        if (!queried.contains(uuid))
            queryModel(profile);
        return null;
    }

    public void tick() {
        if (clearCounter++ > 200) {
            clearCounter = 0;
            Set<UUID> uuids = Sets.newHashSet();
            for (AbstractClientPlayerEntity playerEntity : MinecraftClient.getInstance().world.getPlayers())
                uuids.add(playerEntity.getUuid());

            List<UUID> toClear = modelPacks.entrySet().stream().map(entry -> entry.getKey())
                    .filter(uuid -> !uuids.contains(uuid)).collect(Collectors.toList());
            toClear.forEach(this::releaseModel);
        }
    }
}
