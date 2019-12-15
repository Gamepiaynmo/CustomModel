package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.IModelSelector;
import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.network.PacketQueryConfig;
import com.github.gamepiaynmo.custommodel.network.PacketReplyConfig;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import com.github.gamepiaynmo.custommodel.server.selector.DefaultModelSelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CustomModel implements ModInitializer {
    public static final String MODID = "custommodel";
    public static final String MODEL_DIR = "custom-models";

    public static final Logger LOGGER = LogManager.getLogger();

    private static final Map<UUID, CustomBoundingBox> boundingBoxMap = Maps.newHashMap();
    private static int clearCounter = 0;
    public static MinecraftServer server;

    private static IModelSelector modelSelector = new DefaultModelSelector();

    private static void sendPacket(PlayerEntity player, Identifier id, Packet<?> packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id, buf);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private static Packet<?> formPacket(Identifier id, Packet<?> packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
            return new CustomPayloadS2CPacket(id, buf);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return packet;
        }
    }

    public static void setModelSelector(IModelSelector modelSelector) {
        CustomModel.modelSelector = modelSelector;
    }

    public static CustomBoundingBox getBoundingBoxForPlayer(PlayerEntity playerEntity) {
        return boundingBoxMap.get(PlayerEntity.getUuidFromProfile(playerEntity.getGameProfile()));
    }

    public static void reloadModel(PlayerEntity receiver, boolean broadcast) {
        reloadModel(receiver, PlayerEntity.getUuidFromProfile(receiver.getGameProfile()), broadcast);
    }

    private static void reloadModel(PlayerEntity receiver, UUID uuid, boolean broadcast) {
        ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(uuid);
        GameProfile profile = playerEntity.getGameProfile();
        uuid = PlayerEntity.getUuidFromProfile(profile);

        for (String entry : modelSelector.getModelForPlayer(server, playerEntity)) {
            File modelFile = new File(CustomModel.MODEL_DIR + "/" + entry);

            try {
                if (modelFile.exists()) {
                    PacketModel packetModel = new PacketModel(modelFile, uuid);
                    if (packetModel.success) {
                        boundingBoxMap.put(uuid, CustomBoundingBox.fromFile(modelFile));
                        modelSelector.onModelSelected(server, playerEntity, entry);

                        Packet send = formPacket(PacketModel.ID, packetModel);
                        if (broadcast)
                            playerEntity.getServerWorld().method_14178().sendToOtherNearbyPlayers(playerEntity, send);
                        ServerSidePacketRegistry.INSTANCE.sendToPlayer(receiver, send);
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    public static void selectModel(ServerPlayerEntity playerEntity, String model) {
        GameProfile profile = playerEntity.getGameProfile();
        UUID uuid = PlayerEntity.getUuidFromProfile(profile);
        File modelFile = new File(CustomModel.MODEL_DIR + "/" + model);

        try {
            if (modelFile.exists()) {
                PacketModel packetModel = new PacketModel(modelFile, uuid);
                if (packetModel.success) {
                    boundingBoxMap.put(uuid, CustomBoundingBox.fromFile(modelFile));
                    modelSelector.onModelSelected(server, playerEntity, model);

                    Packet send = formPacket(PacketModel.ID, packetModel);
                    playerEntity.getServerWorld().method_14178().sendToNearbyPlayers(playerEntity, send);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void onInitialize() {
        new File(MODEL_DIR).mkdirs();

        ServerCommand.register();

        ServerSidePacketRegistry.INSTANCE.register(PacketQuery.ID, (context, buffer) -> {
            PacketQuery packet = new PacketQuery();
            try {
                packet.read(buffer);
                context.getTaskQueue().execute(() -> {
                    reloadModel(context.getPlayer(), packet.getPlayerUuid(), false);
                });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });

        ServerSidePacketRegistry.INSTANCE.register(PacketQueryConfig.ID, (context, buffer) -> {
            sendPacket(context.getPlayer(), PacketReplyConfig.ID, new PacketReplyConfig(ModConfig.getConfig()));
        });

        ServerStartCallback.EVENT.register(minecraftServer -> server = minecraftServer);
        ServerStopCallback.EVENT.register(minecraftServer -> server = null);

        ServerTickCallback.EVENT.register(minecraftServer -> {
            if (clearCounter++ > 200) {
                clearCounter = 0;
                Set<UUID> uuids = Sets.newHashSet();
                for (ServerPlayerEntity playerEntity : minecraftServer.getPlayerManager().getPlayerList())
                    uuids.add(PlayerEntity.getUuidFromProfile(playerEntity.getGameProfile()));
                for (Iterator<Map.Entry<UUID, CustomBoundingBox>> iter = boundingBoxMap.entrySet().iterator(); iter.hasNext();) {
                    if (!uuids.contains(iter.next().getKey()))
                        iter.remove();
                }
            }
        });

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
}
