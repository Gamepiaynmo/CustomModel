package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.network.PacketQueryConfig;
import com.github.gamepiaynmo.custommodel.network.PacketReplyConfig;
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
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class CustomModelClient implements ClientModInitializer {
    private static final Map<UUID, ModelPack> modelPacks = Maps.newHashMap();
    private static int clearCounter = 0;

    public static TextureManager textureManager;
    public static Map<String, PlayerEntityRenderer> playerRenderers;

    private static final Set<UUID> queried = Sets.newHashSet();
    public static ModConfig.ServerConfig serverConfig;
    public static boolean configQueried = false;

    public static final Logger LOGGER = LogManager.getLogger();

    public static AbstractClientPlayerEntity currentPlayer;
    public static RenderParameter currentParameter;
    public static PlayerEntityRenderer currentRenderer;
    public static PlayerEntityModel currentModel;
    public static CustomJsonModel currentJsonModel;
    public static Matrix4 currentInvTransform;

    public static boolean isRenderingInventory;
    public static EntityParameter inventoryEntityParameter;
    public static boolean isRenderingFirstPerson;

    private static void sendPacket(Identifier id, Packet<?> packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
            ClientSidePacketRegistry.INSTANCE.sendToServer(id, buf);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public static void clearModels() {
        for (ModelPack pack : modelPacks.values())
            pack.release();
        modelPacks.clear();
        queried.clear();

        serverConfig = null;
        configQueried = false;
    }

    private static void addModel(UUID name, ModelPack pack) {
        ModelPack old = modelPacks.get(name);
        if (old != null)
            old.release();
        modelPacks.put(name, pack);
    }

    private static boolean loadModel(UUID uuid, String model) {
        ModelInfo info = CustomModel.models.get(model);
        ModelPack pack = null;

        try {
            if (info == null)
                throw new ModelNotFoundException(model);
            File modelFile = new File(CustomModel.MODEL_DIR + "/" + model);

            if (modelFile.isDirectory())
                pack = ModelPack.fromDirectory(textureManager, modelFile, uuid);
            if (modelFile.isFile())
                pack = ModelPack.fromZipFile(textureManager, modelFile, uuid);
        } catch (Exception e) {
            MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.CHAT,
                    new TranslatableText("error.custommodel.loadmodelpack", model, e.getMessage()).formatted(Formatting.RED));
            LOGGER.warn(e.getMessage(), e);
        }

        if (pack != null && pack.successfulLoaded()) {
            addModel(uuid, pack);
            return true;
        }

        return false;
    }

    public static void queryModel(GameProfile profile) {
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        queried.add(uuid);

        if (ClientSidePacketRegistry.INSTANCE.canServerReceive(PacketQuery.ID))
            sendPacket(PacketQuery.ID, new PacketQuery(profile));
        else reloadModel(profile);
    }

    public static void selectModel(GameProfile profile, String model) {
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        loadModel(uuid, model);
    }

    public static void reloadModel(GameProfile profile) {
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        if (modelPacks.containsKey(uuid))
            loadModel(uuid, modelPacks.get(uuid).getModel().getModelInfo().modelId);
        else loadModel(uuid, ModConfig.getDefaultModel());
    }

    public static ModelPack getModelForPlayer(AbstractClientPlayerEntity player) {
        GameProfile profile = player.getGameProfile();
        if (profile != null) {
            UUID uuid = PlayerEntity.getUuidFromProfile(profile);

            ModelPack pack = modelPacks.get(uuid);
            if (pack != null)
                return pack;

            if (!queried.contains(uuid))
                queryModel(profile);
        }
        return null;
    }

    public static float getPartial() {
        return ((IPartial) (Object) MinecraftClient.getInstance()).getPartial();
    }

    @Override
    public void onInitializeClient() {
        new File(CustomModel.MODEL_DIR).mkdirs();

        WorldTickCallback.EVENT.register(world -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (world == client.world) {
                for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                    PlayerEntityRenderer renderer = client.getEntityRenderManager().getRenderer(player);
                    ((ICustomPlayerRenderer) renderer).tick(player);
                }

                if (!configQueried && serverConfig == null) {
                    if (ClientSidePacketRegistry.INSTANCE.canServerReceive(PacketQueryConfig.ID)) {
                        sendPacket(PacketQueryConfig.ID, new PacketQueryConfig());
                        configQueried = true;
                    } else {
                        serverConfig = new ModConfig.ServerConfig();
                        serverConfig.customEyeHeight = ModConfig.isCustomEyeHeight();
                        serverConfig.customBoundingBox = false;
                    }
                }

                if (clearCounter++ > 200) {
                    clearCounter = 0;
                    Set<UUID> uuids = Sets.newHashSet();
                    for (AbstractClientPlayerEntity playerEntity : client.world.getPlayers())
                        uuids.add(PlayerEntity.getUuidFromProfile(playerEntity.getGameProfile()));
                    for (Iterator<Map.Entry<UUID, ModelPack>> iter = modelPacks.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry<UUID, ModelPack> entry = iter.next();
                        if (!uuids.contains(entry.getKey())) {
                            entry.getValue().release();
                            queried.remove(entry.getKey());
                            iter.remove();
                        }
                    }
                }
            }
        });

        ClientSidePacketRegistry.INSTANCE.register(PacketModel.ID, (context, buffer) -> {
            PacketModel packet = new PacketModel();
            try {
                packet.read(buffer);
                context.getTaskQueue().execute(() -> {
                    ModelPack pack = null;
                    try {
                        pack = ModelPack.fromZipMemory(textureManager, packet.getUuid(), packet.getData());
                    } catch (Exception e) {
                        MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.CHAT,
                                new TranslatableText("error.custommodel.loadmodelpack", "", e.getMessage()).formatted(Formatting.RED));
                        LOGGER.warn(e.getMessage(), e);
                    }
                    if (pack != null && pack.successfulLoaded())
                        addModel(packet.getUuid(), pack);
                });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });

        ClientSidePacketRegistry.INSTANCE.register(PacketReplyConfig.ID, (context, buffer) -> {
            PacketReplyConfig packet = new PacketReplyConfig();
            try {
                packet.read(buffer);
                serverConfig = packet.getConfig();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });
    }
}
