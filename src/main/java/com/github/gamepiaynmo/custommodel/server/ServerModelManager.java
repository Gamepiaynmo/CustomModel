package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.IModelSelector;
import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.server.selector.DefaultModelSelector;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServerModelManager {
    private final Map<UUID, ModelInfo> modelMap = Maps.newHashMap();
    public final Map<String, ModelLoadInfo> models = Maps.newHashMap();

    private final IModelSelector defaultSelector = new DefaultModelSelector();
    private IModelSelector modelSelector = defaultSelector;

    public void setModelSelector(IModelSelector modelSelector) {
        this.modelSelector = modelSelector;
        if (modelSelector == null)
            this.modelSelector = defaultSelector;
    }

    public IModelSelector getModelSelector() {
        return modelSelector;
    }

    public ModelInfo getModelForPlayer(PlayerEntity playerEntity) {
        return modelMap.get(playerEntity.getUuid());
    }

    public ModelInfo getModelForPlayer(GameProfile profile) {
        return modelMap.get(PlayerEntity.getUuidFromProfile(profile));
    }

    public void refreshModelList() {
        models.clear();
        for (File file : new File(CustomModel.MODEL_DIR).listFiles()) {
            try {
                ModelInfo info = ModelInfo.fromFile(file);
                ModelLoadInfo old = models.get(info.modelId);
                if (old == null || info.version.compareTo(old.info.version) > 0)
                    models.put(info.modelId, new ModelLoadInfo(info, false));
            } catch (Exception e) {
                CustomModel.LOGGER.warn(e.getMessage(), e);
            }
        }

        for (ModelInfo info : modelMap.values()) {
            models.putIfAbsent(info.modelId, new ModelLoadInfo(info, true));
            models.get(info.modelId).refCnt++;
        }
    }

    public void addModelInfo(ModelInfo info, ServerPlayerEntity sender, UUID receiverUuid) {
        ModelLoadInfo old = models.get(info.modelId);
        if (ModConfig.isReceiveModels() && (old == null || sender.getUuid().equals(old.info.sender))) {
            ModelLoadInfo newInfo = new ModelLoadInfo(info, true);
            if (old != null)
                newInfo.refCnt = old.refCnt;
            models.put(info.modelId, newInfo);

            ServerPlayerEntity receiver = CustomModel.server.getPlayerManager().getPlayer(receiverUuid);
            if (receiver != null)
                selectModel(sender, receiver, info.modelId);
        }
    }

    public Collection<Text> getServerModelInfoList() {
        return models.values().stream().filter(info -> !info.isClient).map(info -> info.text).collect(Collectors.toList());
    }

    public Collection<String> getServerModelIdList() {
        return models.values().stream().filter(info -> !info.isClient).map(info -> info.info.modelId).collect(Collectors.toList());
    }

    public Collection<String> getModelIdList() {
        return models.values().stream().map(info -> info.info.modelId).collect(Collectors.toList());
    }

    public void clearModel(ServerPlayerEntity playerEntity) {
        GameProfile profile = playerEntity.getGameProfile();
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        ModelInfo old = modelMap.remove(uuid);
        if (old != null)
            decRefCnt(old);
        modelSelector.clearModelForPlayer(profile);
        Packet packet = CustomModel.formPacket(PacketModel.ID, new PacketModel(uuid));
        playerEntity.getServerWorld().method_14178().sendToNearbyPlayers(playerEntity, packet);
    }

    private void decRefCnt(ModelInfo modelInfo) {
        ModelLoadInfo info = models.get(modelInfo.modelId);
        info.refCnt--;
        if (info.refCnt < 0)
            CustomModel.LOGGER.warn(info.info.modelId + " reference count < 0");
        if (info.refCnt == 0 && info.isClient)
            models.remove(info.info.modelId);
    }

    private void setModel(UUID uuid, ModelLoadInfo info) {
        info.refCnt++;
        ModelInfo old = modelMap.put(uuid, info.info);
        if (old != null)
            decRefCnt(old);
    }

    public void onPlayerExit(ServerPlayerEntity player) {
        ModelInfo info = modelMap.remove(player.getUuid());
        if (info != null)
            decRefCnt(info);
    }

    public void reloadModel(PlayerEntity receiver, boolean broadcast) throws LoadModelException {
        reloadModel(receiver, PlayerEntity.getUuidFromProfile(receiver.getGameProfile()), broadcast);
    }

    public void reloadModel(PlayerEntity receiver, UUID uuid, boolean broadcast) throws LoadModelException {
        ServerPlayerEntity playerEntity = CustomModel.server.getPlayerManager().getPlayer(uuid);
        GameProfile profile = playerEntity.getGameProfile();
        uuid = PlayerEntity.getUuidFromProfile(profile);

        String entry = modelSelector.getModelForPlayer(profile);
        ModelLoadInfo info = models.get(entry);
        if (info == null || receiver.getUuid().equals(info.info.sender)) {
            if (ModConfig.isReceiveModels()) {
                CustomModel.sendPacket(receiver, PacketQuery.ID, new PacketQuery(uuid, entry));
                return;
            }
            throw new ModelNotFoundException(entry);
        }

        try {
            PacketModel packetModel = info.info.getPacket(uuid);
            if (packetModel.success) {
                setModel(uuid, info);
                modelSelector.setModelForPlayer(profile, entry);

                Packet send = CustomModel.formPacket(PacketModel.ID, packetModel);
                if (broadcast)
                    playerEntity.getServerWorld().method_14178().sendToOtherNearbyPlayers(playerEntity, send);
                ((ServerPlayerEntity) receiver).networkHandler.sendPacket(send);
            }
        } catch (Exception e) {
            CustomModel.LOGGER.warn(e.getMessage(), e);
            throw new LoadModelException(entry, e);
        }
    }

    public void selectModel(ServerPlayerEntity playerEntity, String model) throws LoadModelException {
        selectModel(null, playerEntity, model);
    }

    public void selectModel(ServerPlayerEntity sender, ServerPlayerEntity playerEntity, String model) throws LoadModelException {
        GameProfile profile = playerEntity.getGameProfile();
        UUID uuid = playerEntity.getUuid();
        ModelLoadInfo info = models.get(model);
        if (info == null) {
            if (ModConfig.isReceiveModels() && sender != null) {
                CustomModel.sendPacket(sender, PacketQuery.ID, new PacketQuery(uuid, model));
                return;
            }
            throw new ModelNotFoundException(model);
        }

        try {
            PacketModel packetModel = info.info.getPacket(uuid);
            if (packetModel.success) {
                setModel(uuid, info);
                modelSelector.setModelForPlayer(profile, model);

                Packet send = CustomModel.formPacket(PacketModel.ID, packetModel);
                playerEntity.getServerWorld().method_14178().sendToNearbyPlayers(playerEntity, send);
            }
        } catch (Exception e) {
            CustomModel.LOGGER.warn(e.getMessage(), e);
            throw new LoadModelException(model, e);
        }
    }
}
