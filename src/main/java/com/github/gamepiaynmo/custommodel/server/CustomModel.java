package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.network.PacketModel;
import com.github.gamepiaynmo.custommodel.network.PacketQuery;
import com.github.gamepiaynmo.custommodel.network.PacketQueryConfig;
import com.github.gamepiaynmo.custommodel.network.PacketReplyConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomModel implements ModInitializer {
    public static final String MODID = "custommodel";
    public static final String MODEL_DIR = "custom-models";

    public static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, CustomBoundingBox> boundingBoxMap = Maps.newHashMap();

    private static void sendPacket(PlayerEntity player, Identifier id, Packet<?> packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id, buf);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public static CustomBoundingBox getBoundingBoxForPlayer(PlayerEntity playerEntity) {
        return boundingBoxMap.get(PlayerEntity.getUuidFromProfile(playerEntity.getGameProfile()).toString().toLowerCase());
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
                    PlayerEntity player = context.getPlayer();
                    GameProfile profile = player.world.getPlayerByUuid(packet.getPlayerUuid()).getGameProfile();

                    String nameEntry = profile.getName().toLowerCase();
                    UUID uuid = PlayerEntity.getUuidFromProfile(profile);
                    String uuidEntry = uuid.toString().toLowerCase();
                    List<String> files = ImmutableList.of(nameEntry, uuidEntry, nameEntry + ".zip", uuidEntry + ".zip");

                    for (String entry : files) {
                        File modelFile = new File(CustomModel.MODEL_DIR + "/" + entry);

                        try {
                            if (modelFile.exists()) {
                                PacketModel packetModel = new PacketModel(modelFile, uuidEntry);
                                if (packetModel.success) {
                                    boundingBoxMap.put(uuidEntry, CustomBoundingBox.fromFile(modelFile));
                                    sendPacket(player, PacketModel.ID, packetModel);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.warn(e.getMessage(), e);
                        }
                    }
                });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });

        ServerSidePacketRegistry.INSTANCE.register(PacketQueryConfig.ID, (context, buffer) -> {
            sendPacket(context.getPlayer(), PacketReplyConfig.ID, new PacketReplyConfig(ModConfig.getConfig()));
        });

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
}
