package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.api.IModelSelector;
import com.github.gamepiaynmo.custommodel.network.*;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Lists;
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
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import com.github.gamepiaynmo.custommodel.server.selector.DefaultModelSelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class CustomModel implements ModInitializer {
    public static final String MODID = "custommodel";
    public static final String MODEL_DIR = "custom-models";

    public static final Logger LOGGER = LogManager.getLogger();

    public static MinecraftServer server;
    public static final ServerModelManager manager = new ServerModelManager();

    public static void sendPacket(PlayerEntity player, Identifier id, IPacket packet) {
        if (ServerSidePacketRegistry.INSTANCE.canPlayerReceive(player, id)) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            try {
                packet.write(buf);
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id, buf);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    static Packet<?> formPacket(Identifier id, IPacket packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return new CustomPayloadS2CPacket(id, buf);
    }

    public static <T extends IPacket> void registerPacket(Identifier id, Class<T> packetClass, Class<? extends IPacketHandler<T>> handlerClass) {
        ServerSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> {
            try {
                IPacket packet = packetClass.newInstance();
                packet.read(buffer);
                context.getTaskQueue().execute(() -> {
                    try {
                        IPacketHandler handler = handlerClass.newInstance();
                        handler.apply(packet, context);
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });
    }

    public static void onPlayerLoggedIn(ServerPlayerEntity playerEntity) {
        playerEntity.networkHandler.sendPacket(formPacket(PacketConfig.ID, new PacketConfig(ModConfig.getConfig().server)));
    }

    public static void onPlayerLoggedOut(ServerPlayerEntity playerEntity) {
        manager.onPlayerExit(playerEntity);
    }

    @Override
    public void onInitialize() {
        new File(MODEL_DIR).mkdirs();

        ServerCommand.register();

        registerPacket(PacketModel.ID, PacketModel.class, PacketModel.Server.class);
        registerPacket(PacketQuery.ID, PacketQuery.class, PacketQuery.Server.class);
        registerPacket(PacketList.ID, PacketList.class, PacketList.class);

        ServerStartCallback.EVENT.register(server -> CustomModel.server = server);
        ServerStopCallback.EVENT.register(server -> CustomModel.server = null);

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        manager.refreshModelList();
    }
}
