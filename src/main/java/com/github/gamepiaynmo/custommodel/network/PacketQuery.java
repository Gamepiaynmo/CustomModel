package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PacketQuery implements IMessage {
    private UUID playerUuid = new UUID(0, 0);
    private String modelId = "";

    public PacketQuery(UUID uuid) {
        playerUuid = uuid;
    }
    public PacketQuery(UUID uuid, String id) {
        this(uuid);
        modelId = id;
    }

    public PacketQuery() {}

    public UUID getPlayerUuid() {
        return playerUuid;
    }
    public String getModelId() { return modelId; }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        playerUuid = buffer.readUniqueId();
        modelId = buffer.readString(64);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeUniqueId(playerUuid);
        buffer.writeString(modelId);
    }

    public static class Server implements IMessageHandler<PacketQuery, IMessage> {
        @Override
        public IMessage onMessage(PacketQuery message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                try {
                    CustomModel.manager.reloadModel(ctx.getServerHandler().player, message.getPlayerUuid(), false);
                } catch (LoadModelException e) {
                    CustomModel.LOGGER.warn(e.getMessage(), e);
                }
            });
            return null;
        }
    }

    public static class Client implements IMessageHandler<PacketQuery, IMessage> {
        @Override
        public IMessage onMessage(PacketQuery message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    CustomModelClient.manager.loadModel(message.getPlayerUuid(), message.getModelId());
                } catch (ModelNotFoundException e) {
                    CustomModelClient.LOGGER.warn(e.getMessage(), e);
                }
            });
            return null;
        }
    }
}
