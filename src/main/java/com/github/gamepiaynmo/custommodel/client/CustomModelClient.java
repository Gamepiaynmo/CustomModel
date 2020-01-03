package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.entity.CustomModelFemaleNpc;
import com.github.gamepiaynmo.custommodel.entity.CustomModelMaleNpc;
import com.github.gamepiaynmo.custommodel.entity.CustomModelNpc;
import com.github.gamepiaynmo.custommodel.entity.NpcHelper;
import com.github.gamepiaynmo.custommodel.mixin.RenderPlayerHandler;
import com.github.gamepiaynmo.custommodel.network.*;
import com.github.gamepiaynmo.custommodel.render.*;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class CustomModelClient {
    private static final Map<UUID, ModelPack> modelPacks = Maps.newHashMap();
    private static int clearCounter = 0;

    private static final Set<UUID> queried = Sets.newHashSet();
    public static ModConfig.ServerConfig serverConfig;
    public static boolean isServerModded = false;

    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean isRenderingInventory;
    public static EntityParameter inventoryEntityParameter;
    public static boolean isRenderingFirstPerson;

    public static void addModel(UUID name, ModelPack pack) {
        ModelPack old = modelPacks.get(name);
        if (old != null)
            old.release();
        modelPacks.put(name, pack);
    }

    public static void clearModels() {
        for (ModelPack pack : modelPacks.values())
            pack.release();
        modelPacks.clear();
        queried.clear();
    }

    public static void clearModel(GameProfile profile) {
        clearModel(EntityPlayer.getUUID(profile));
    }

    public static void clearModel(UUID uuid) {
        EntityPlayer entityPlayer = Minecraft.getMinecraft().world.getPlayerEntityByUUID(uuid);
        if (entityPlayer != null)
            CustomModel.getModelSelector().clearModelForPlayer(entityPlayer.getGameProfile());
        ModelPack old = modelPacks.remove(uuid);
        if (old != null)
            old.release();
    }

    private static boolean loadModel(UUID uuid, String model) {
        EntityPlayer entityPlayer = Minecraft.getMinecraft().world.getPlayerEntityByUUID(uuid);
        ModelInfo info = CustomModel.models.get(model);
        ModelPack pack = null;

        try {
            if (info == null)
                throw new ModelNotFoundException(model);
            File modelFile = new File(CustomModel.MODEL_DIR + "/" + info.fileName);
            TextureManager textureManager = Minecraft.getMinecraft().renderEngine;

            if (modelFile.isDirectory())
                pack = ModelPack.fromDirectory(textureManager, modelFile, uuid);
            if (modelFile.isFile())
                pack = ModelPack.fromZipFile(textureManager, modelFile, uuid);
        } catch (ModelNotFoundException e) {
            if (!model.equals(ModConfig.getDefaultModel()))
                throw e;
        } catch (Exception e) {
            ITextComponent text = new TextComponentTranslation("error.custommodel.loadmodelpack", model, e.getMessage());
            text.getStyle().setColor(TextFormatting.RED);
            Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.CHAT, text);
            LOGGER.warn(e.getMessage(), e);
        }

        if (pack != null && pack.successfulLoaded()) {
            addModel(uuid, pack);
            if (entityPlayer != null)
                CustomModel.getModelSelector().setModelForPlayer(entityPlayer.getGameProfile(), model);
            return true;
        }

        return false;
    }

    public static void queryModel(GameProfile profile) {
        UUID uuid = EntityPlayer.getUUID(profile);

        queried.add(uuid);
        if (isServerModded)
            NetworkHandler.CHANNEL.sendToServer(new PacketQuery(uuid));
        else reloadModel(profile);
    }

    public static void queryModel(UUID uuid) {
        queried.add(uuid);
        if (isServerModded)
            NetworkHandler.CHANNEL.sendToServer(new PacketQuery(uuid));
    }

    public static void selectModel(GameProfile profile, String model) {
        UUID uuid = EntityPlayer.getUUID(profile);
        loadModel(uuid, model);
    }

    public static void reloadModel(GameProfile profile) {
        UUID uuid = EntityPlayer.getUUID(profile);
        loadModel(uuid, CustomModel.getModelSelector().getModelForPlayer(profile));
    }

    public static ModelPack getModelForEntity(EntityLivingBase entity) {
        if (entity instanceof AbstractClientPlayer)
            return getModelForPlayer(((AbstractClientPlayer) entity));

        entity = NpcHelper.getParent(entity);
        if (entity != null) {
            UUID uuid = entity.getUniqueID();
            ModelPack pack = modelPacks.get(uuid);
            if (pack != null)
                return pack;

            if (!queried.contains(uuid))
                queryModel(uuid);
        }
        return null;
    }

    public static ModelPack getModelForPlayer(AbstractClientPlayer player) {
        GameProfile profile = player.getGameProfile();
        if (profile != null) {
            UUID uuid = EntityPlayer.getUUID(profile);

            ModelPack pack = modelPacks.get(uuid);
            if (pack != null)
                return pack;

            if (!queried.contains(uuid))
                queryModel(profile);
        }
        return null;
    }

    private static float lastPartial;
    public static float getPartial() {
        return Minecraft.getMinecraft().isGamePaused() ? lastPartial : (lastPartial = Minecraft.getMinecraft().getRenderPartialTicks());
    }

    public static void onInitializeClient() {
        new File(CustomModel.MODEL_DIR).mkdirs();
        serverConfig = new ModConfig.ServerConfig();
        serverConfig.customEyeHeight = ModConfig.isCustomEyeHeight();

        MinecraftForge.EVENT_BUS.register(CustomModelClient.class);
        MinecraftForge.EVENT_BUS.register(RenderPlayerHandler.class);
        ClientCommandHandler.instance.registerCommand(new Command());
    }

    public static void initPlayerRenderer() {
        for (RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values())
            RenderPlayerHandler.customize(renderer);
        if (CustomModel.hasnpc) {
            RenderingRegistry.registerEntityRenderingHandler(CustomModelMaleNpc.class, new RenderNpc(new ModelPlayer(0, false), false));
            RenderingRegistry.registerEntityRenderingHandler(CustomModelFemaleNpc.class, new RenderNpc(new ModelPlayer(0, true), true));
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ClientTickEvent event) {
        WorldClient world = Minecraft.getMinecraft().world;
        if (world != null && !Minecraft.getMinecraft().isGamePaused() && event.phase == TickEvent.Phase.END) {
            for (AbstractClientPlayer player : world.getPlayers(AbstractClientPlayer.class, player -> true)) {
                RenderPlayerHandler.tick(player);
            }

            if (CustomModel.hasnpc) {
                for (EntityLivingBase entity : NpcHelper.getCustomModelNpcs(world)) {
                    RenderPlayerHandler.tick(entity);
                }
            }

            if (clearCounter++ > 200) {
                clearCounter = 0;
                Set<UUID> uuids = Sets.newHashSet();
                for (AbstractClientPlayer playerEntity : world.getPlayers(AbstractClientPlayer.class, player -> true))
                    uuids.add(EntityPlayer.getUUID(playerEntity.getGameProfile()));
                if (CustomModel.hasnpc)
                    uuids.addAll(NpcHelper.getNpcUUIDs(world));
                for (Iterator<Map.Entry<UUID, ModelPack>> iter = modelPacks.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry<UUID, ModelPack> entry = iter.next();
                    if (!uuids.contains(entry.getKey())) {
                        entry.getValue().release();
                        queried.remove(entry.getKey());
                        iter.remove();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (event.getCommand() instanceof Command && isServerModded)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        for (ModelPack pack : modelPacks.values())
            pack.release();
        modelPacks.clear();
        queried.clear();

        serverConfig = new ModConfig.ServerConfig();
        serverConfig.customEyeHeight = ModConfig.isCustomEyeHeight();
        serverConfig.customBoundingBox = false;
        isServerModded = false;
    }
}
