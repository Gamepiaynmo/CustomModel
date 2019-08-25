package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.render.RenderParameter;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.network.PacketReload;
import com.github.gamepiaynmo.custommodel.render.CustomPlayerEntityRenderer;
import com.google.common.collect.*;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

public class CustomModelClient implements ClientModInitializer {
    private static final Map<String, ModelPack> modelPacks = Maps.newHashMap();

    public static TextureManager textureManager;
    public static CustomPlayerEntityRenderer customRenderer;

    private static final Set<GameProfile> queried = Sets.newHashSet();

    public static final Logger LOGGER = LogManager.getLogger();

    public static AbstractClientPlayerEntity currentPlayer;
    public static RenderParameter currentParameter;
    public static CustomPlayerEntityRenderer currentRenderer;
    public static PlayerEntityModel currentModel;

    private static void sendPacket(Identifier id, Packet<?> packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
            ClientSidePacketRegistry.INSTANCE.sendToServer(id, buf);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private static void clearModel(GameProfile profile) {
        String nameEntry = profile.getName().toLowerCase();
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        String uuidEntry = uuid.toString().toLowerCase();
        List<String> files = ImmutableList.of(nameEntry, uuidEntry);
        queried.remove(profile);

        for (String entry : files) {
            ModelPack pack = modelPacks.get(nameEntry);
            if (pack != null) {
                modelPacks.remove(nameEntry);
                pack.release();
            }
        }
    }

    private static void reloadModel(GameProfile profile) {
        String nameEntry = profile.getName().toLowerCase();
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        String uuidEntry = uuid.toString().toLowerCase();
        List<String> files = ImmutableList.of(nameEntry, uuidEntry, nameEntry + ".zip", uuidEntry + ".zip");
        queried.add(profile);

        ModelPack pack = null;
        for (String entry : files) {
            File modelFile = new File(CustomModel.MODEL_DIR + "/" + entry);

            try {
                if (modelFile.isDirectory())
                    pack = ModelPack.fromDirectory(textureManager, modelFile);
                if (modelFile.isFile())
                    pack = ModelPack.fromZipFile(textureManager, modelFile);
            } catch (Exception e) {
                MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.CHAT,
                        new TranslatableText("error.custommodel.loadmodelpack", e.getMessage()));
                LOGGER.warn(e.getMessage(), e);
            }

            if (pack != null && pack.successfulLoaded()) {
                modelPacks.put(pack.getDirName(), pack);
                break;
            }
        }

        if (!ModConfig.isClientFirst() && ClientSidePacketRegistry.INSTANCE.canServerReceive(PacketQuery.ID));
            sendPacket(PacketQuery.ID, new PacketQuery(profile));
    }

    public static ModelPack getModelForPlayer(AbstractClientPlayerEntity player) {
        GameProfile profile = player.getGameProfile();
        String nameEntry = profile.getName().toLowerCase();
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        String uuidEntry = uuid.toString().toLowerCase();
        List<String> names = ImmutableList.of(nameEntry, uuidEntry);
//        List<String> names = ImmutableList.of(nameEntry, nameEntry + ".zip", uuidEntry, uuidEntry + ".zip");

        for (String name : names) {
            ModelPack pack = modelPacks.get(nameEntry);
            if (pack != null)
                return pack;
        }

        if (!queried.contains(profile))
            reloadModel(profile);
        return null;
    }

    @Override
    public void onInitializeClient() {
        new File(CustomModel.MODEL_DIR).mkdirs();

        WorldTickCallback.EVENT.register(world -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (world == client.world) {
                for (AbstractClientPlayerEntity player : client.world.getPlayers())
                    customRenderer.tick(player);
            }
        });

        ClientSidePacketRegistry.INSTANCE.register(PacketReload.ID, (context, buffer) -> {
            PacketReload packet = new PacketReload();
            try {
                packet.read(buffer);
                context.getTaskQueue().execute(() -> {
                    ClientWorld world = MinecraftClient.getInstance().world;
                    if (world != null) {
                        for (UUID uuid : packet.getUUIDs()) {
                            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) world.getPlayerByUuid(uuid);
                            clearModel(player.getGameProfile());
                        }
                    }
                });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });

        ClientSidePacketRegistry.INSTANCE.register(PacketModel.ID, (context, buffer) -> {
            PacketModel packet = new PacketModel();
            try {
                packet.read(buffer);
                context.getTaskQueue().execute(() -> {
                    ModelPack pack = null;
                    try {
                        pack = ModelPack.fromZipMemory(textureManager, packet.getName(), packet.getData());
                    } catch (Exception e) {
                        MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.CHAT,
                                new TranslatableText("error.custommodel.loadmodelpack", e.getMessage()));
                        LOGGER.warn(e.getMessage(), e);
                    }
                    if (pack != null && pack.successfulLoaded())
                        modelPacks.put(pack.getDirName(), pack);
                });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
}
