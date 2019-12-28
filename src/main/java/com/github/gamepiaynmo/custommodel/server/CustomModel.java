package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.IModelSelector;
import com.github.gamepiaynmo.custommodel.mixin.PlayerStatureHandler;
import com.github.gamepiaynmo.custommodel.network.*;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import com.github.gamepiaynmo.custommodel.server.selector.DefaultModelSelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class CustomModel {
    public static final String MODID = "custommodel";
    public static final String MODEL_DIR = "custom-models";

    public static final Logger LOGGER = LogManager.getLogger();

    private static final Map<UUID, ModelInfo> modelMap = Maps.newHashMap();
    private static int clearCounter = 0;
    public static MinecraftServer server;
    public static final Map<String, ModelInfo> models = Maps.newHashMap();

    private static final IModelSelector defaultSelector = new DefaultModelSelector();
    private static IModelSelector modelSelector = defaultSelector;

    public static void setModelSelector(IModelSelector modelSelector) {
        CustomModel.modelSelector = modelSelector;
        if (modelSelector == null)
            CustomModel.modelSelector = defaultSelector;
    }

    public static IModelSelector getModelSelector() {
        return CustomModel.modelSelector;
    }

    public static ModelInfo getBoundingBoxForPlayer(EntityPlayer playerEntity) {
        return modelMap.get(EntityPlayer.getUUID(playerEntity.getGameProfile()));
    }

    public static void refreshModelList() {
        models.clear();
        for (File file : new File(CustomModel.MODEL_DIR).listFiles()) {
            try {
                ModelInfo info = ModelInfo.fromFile(file);
                ModelInfo old = models.get(info.modelId);
                if (old == null || info.version.compareTo(old.version) > 0)
                    models.put(info.modelId, info);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    public static Collection<ITextComponent> getModelInfoList() {
        List<ITextComponent> res = Lists.newArrayList();
        for (Map.Entry<String, ModelInfo> entry : models.entrySet()) {
            ModelInfo info = entry.getValue();
            ITextComponent text = new TextComponentString(info.modelName);
            Style style = text.getStyle();

            TextComponentTranslation hoverText = new TextComponentTranslation("text.custommodel.modelinfo.name", info.modelName);
            hoverText.appendText("\n").appendSibling(new TextComponentTranslation("text.custommodel.modelinfo.id", info.modelId));
            if (info.version.length() > 0)
                hoverText.appendText("\n").appendSibling(new TextComponentTranslation("text.custommodel.modelinfo.version", info.version));
            if (info.author.length() > 0)
                hoverText.appendText("\n").appendSibling(new TextComponentTranslation("text.custommodel.modelinfo.author", info.author));

            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/" + CustomModel.MODID + " select " + info.modelId));
            res.add(text);
        }
        return res;
    }

    public static Collection<String> getModelIdList() {
        List<String> res = Lists.newArrayList();
        for (Map.Entry<String, ModelInfo> entry : models.entrySet())
            res.add(entry.getKey());
        return res;
    }

    public static void clearModel(EntityPlayerMP playerEntity) {
        GameProfile profile = playerEntity.getGameProfile();
        UUID uuid = EntityPlayer.getUUID(profile);
        modelMap.remove(uuid);
        modelSelector.clearModelForPlayer(profile);
        PacketModel packetModel = new PacketModel(uuid);
        NetworkHandler.CHANNEL.sendToAllTracking(packetModel, playerEntity);
        NetworkHandler.CHANNEL.sendTo(packetModel, playerEntity);
    }

    public static void reloadModel(EntityPlayerMP receiver, boolean broadcast) throws LoadModelException {
        reloadModel(receiver, EntityPlayer.getUUID(receiver.getGameProfile()), broadcast);
    }

    public static void reloadModel(EntityPlayerMP receiver, UUID uuid, boolean broadcast) throws LoadModelException {
        EntityPlayerMP playerEntity = server.getPlayerList().getPlayerByUUID(uuid);
        GameProfile profile = playerEntity.getGameProfile();
        uuid = EntityPlayer.getUUID(profile);

        String entry = modelSelector.getModelForPlayer(profile);
        ModelInfo info = models.get(entry);

        try {
            if (info == null)
                throw new ModelNotFoundException(entry);
            File modelFile = new File(CustomModel.MODEL_DIR + "/" + info.fileName);

            if (modelFile.exists()) {
                PacketModel packetModel = new PacketModel(modelFile, uuid);
                if (packetModel.success) {
                    modelMap.put(uuid, ModelInfo.fromFile(modelFile));
                    modelSelector.setModelForPlayer(profile, entry);

                    if (!broadcast)
                        NetworkHandler.CHANNEL.sendTo(packetModel, receiver);
                    else {
                        NetworkHandler.CHANNEL.sendToAllTracking(packetModel, playerEntity);
                        NetworkHandler.CHANNEL.sendTo(packetModel, playerEntity);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            throw new LoadModelException(entry, e);
        }
    }

    public static void selectModel(EntityPlayerMP playerEntity, String model) throws LoadModelException {
        GameProfile profile = playerEntity.getGameProfile();
        UUID uuid = EntityPlayer.getUUID(profile);
        ModelInfo info = models.get(model);
        if (info == null)
            throw new ModelNotFoundException(model);
        File modelFile = new File(CustomModel.MODEL_DIR + "/" + info.fileName);

        try {
            if (modelFile.exists()) {
                PacketModel packetModel = new PacketModel(modelFile, uuid);
                if (packetModel.success) {
                    modelMap.put(uuid, ModelInfo.fromFile(modelFile));
                    modelSelector.setModelForPlayer(profile, model);
                    NetworkHandler.CHANNEL.sendToAllTracking(packetModel, playerEntity);
                    NetworkHandler.CHANNEL.sendTo(packetModel, playerEntity);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            throw new LoadModelException(model, e);
        }
    }

    public static void onInitialize() {
        new File(MODEL_DIR).mkdirs();
        ModConfig.updateConfig();
        refreshModelList();

        MinecraftForge.EVENT_BUS.register(CustomModel.class);
        MinecraftForge.EVENT_BUS.register(PlayerStatureHandler.class);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (clearCounter++ > 200) {
            clearCounter = 0;
            Set<UUID> uuids = Sets.newHashSet();
            for (EntityPlayerMP playerEntity : server.getPlayerList().getPlayers())
                uuids.add(EntityPlayer.getUUID(playerEntity.getGameProfile()));
            for (Iterator<Map.Entry<UUID, ModelInfo>> iter = modelMap.entrySet().iterator(); iter.hasNext();) {
                if (!uuids.contains(iter.next().getKey()))
                    iter.remove();
            }
        }
    }

    static private List<EntityPlayerMP> toSendConfigList = Lists.newArrayList();

    @SubscribeEvent
    public static void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
            if (toSendConfigList.contains(event.player)) {
                NetworkHandler.CHANNEL.sendTo(new PacketReplyConfig(), (EntityPlayerMP) event.player);
                toSendConfigList.remove(event.player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        toSendConfigList.add((EntityPlayerMP) event.player);
    }

    public static void onServerStart(MinecraftServer server) {
        CustomModel.server = server;
        ((CommandHandler) server.commandManager).registerCommand(new ServerCommand());
    }

    public static void onServerStop() {
        CustomModel.server = null;
    }
}
