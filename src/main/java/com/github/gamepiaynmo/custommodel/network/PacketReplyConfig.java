package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class PacketReplyConfig implements IMessage, IMessageHandler<PacketReplyConfig, IMessage> {
    private ModConfig.ServerConfig config;

    public PacketReplyConfig() {
        this.config = new ModConfig.ServerConfig();
    }

    public PacketReplyConfig(ModConfig.ServerConfig config) {
        this.config = config;
    }

    public ModConfig.ServerConfig getConfig() {
        return config;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        config.customEyeHeight = buf.readBoolean();
        config.customBoundingBox = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(config.customEyeHeight);
        buf.writeBoolean(config.customBoundingBox);
    }

    @Override
    public IMessage onMessage(PacketReplyConfig message, MessageContext ctx) {
        CustomModelClient.serverConfig = message.getConfig();
        CustomModelClient.isServerModded = true;
        return null;
    }
}
