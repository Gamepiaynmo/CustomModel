package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.entity.NpcHelper;
import com.github.gamepiaynmo.custommodel.network.NetworkHandler;
import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.server.ModelLoadInfo;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ClientModelManager {
    private static final Set<UUID> queried = Sets.newHashSet();
    private final Map<UUID, ModelPack> modelPacks = Maps.newHashMap();
    private int clearCounter = 0;

    public void addModel(UUID name, ModelPack pack) {
        ModelPack old = modelPacks.get(name);
        if (old != null)
            old.release();
        modelPacks.put(name, pack);
    }

    private EntityPlayer getPlayer(UUID uuid) {
        return Minecraft.getMinecraft().world.getPlayerEntityByUUID(uuid);
    }

    public void clearModels() {
        for (ModelPack pack : modelPacks.values())
            pack.release();
        modelPacks.clear();
        queried.clear();
    }

    public void clearModel(GameProfile profile) {
        clearModel(EntityPlayer.getUUID(profile));
    }

    public void clearModel(UUID uuid) {
        EntityPlayer entityPlayer = Minecraft.getMinecraft().world.getPlayerEntityByUUID(uuid);
        if (entityPlayer != null)
            CustomModel.manager.getModelSelector().clearModelForPlayer(entityPlayer.getGameProfile());
        ModelPack old = modelPacks.remove(uuid);
        if (old != null)
            old.release();
        queried.remove(uuid);
    }

    public boolean loadModel(UUID uuid, String model) {
        ModelLoadInfo info = CustomModel.manager.models.get(model);
        ModelPack pack = null;

        try {
            if (info == null)
                throw new ModelNotFoundException(model);
            TextureManager textureManager = Minecraft.getMinecraft().renderEngine;
            pack = ModelPack.fromModelInfo(textureManager, info.info, uuid);

            if (ModConfig.isSendModels() && CustomModelClient.isServerModded && CustomModelClient.serverConfig.receiveModels) {
                PacketModel packetModel = info.info.getPacket(uuid);
                if (packetModel.success)
                    NetworkHandler.CHANNEL.sendToServer(packetModel);
            }
        } catch (ModelNotFoundException e) {
            if (!model.equals(ModConfig.getDefaultModel()))
                throw e;
        } catch (Exception e) {
            ITextComponent text = new TextComponentTranslation("error.custommodel.loadmodelpack", model, e.getMessage());
            text.getStyle().setColor(TextFormatting.RED);
            Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.CHAT, text);
            CustomModelClient.LOGGER.warn(e.getMessage(), e);
        }

        if (pack != null && pack.successfulLoaded()) {
            addModel(uuid, pack);

            EntityPlayer entityPlayer = getPlayer(uuid);
            if (entityPlayer != null)
                CustomModel.manager.getModelSelector().setModelForPlayer(entityPlayer.getGameProfile(), model);
            return true;
        }

        return false;
    }

    public void queryModel(GameProfile profile) {
        queryModel(EntityPlayer.getUUID(profile));
    }

    public void queryModel(UUID uuid) {
        EntityPlayer player = getPlayer(uuid);

        queried.add(uuid);
        if (CustomModelClient.isServerModded)
            NetworkHandler.CHANNEL.sendToServer(new PacketQuery(uuid));
        else if (player != null)
            reloadModel(player.getGameProfile());
    }

    public boolean selectModel(GameProfile profile, String model) {
        UUID uuid = EntityPlayer.getUUID(profile);
        return loadModel(uuid, model);
    }

    public boolean reloadModel(GameProfile profile) {
        UUID uuid = EntityPlayer.getUUID(profile);
        return loadModel(uuid, CustomModel.manager.getModelSelector().getModelForPlayer(profile));
    }

    public ModelPack getModelForPlayer(AbstractClientPlayer player) {
        return getModelForEntity(player.getUniqueID());
    }

    public ModelPack getModelForEntity(EntityLivingBase entity) {
        if (entity instanceof AbstractClientPlayer)
            return getModelForPlayer(((AbstractClientPlayer) entity));

        entity = NpcHelper.getParent(entity);
        if (entity != null)
            return getModelForEntity(entity.getUniqueID());
        return null;
    }

    private ModelPack getModelForEntity(UUID uuid) {
        ModelPack pack = modelPacks.get(uuid);
        if (pack != null)
            return pack;

        if (!queried.contains(uuid))
            queryModel(uuid);
        return null;
    }

    public void tick() {
        World world = Minecraft.getMinecraft().world;
        if (clearCounter++ > 200) {
            clearCounter = 0;
            Set<UUID> uuids = Sets.newHashSet();
            for (AbstractClientPlayer playerEntity : world.getPlayers(AbstractClientPlayer.class, player -> true))
                uuids.add(playerEntity.getUniqueID());
            if (CustomModel.hasnpc)
                uuids.addAll(NpcHelper.getNpcUUIDs(world));

            List<UUID> toClear = modelPacks.entrySet().stream().map(entry -> entry.getKey())
                    .filter(uuid -> !uuids.contains(uuid)).collect(Collectors.toList());
            toClear.forEach(this::clearModel);
        }
    }
}
