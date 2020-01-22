package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketConfig implements IMessage, IMessageHandler<PacketConfig, IMessage> {
    private ModConfig.ServerConfig config;

    public PacketConfig() {
        this.config = new ModConfig.ServerConfig();
    }

    public PacketConfig(ModConfig.ServerConfig config) {
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
    public IMessage onMessage(PacketConfig message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            CustomModelClient.serverConfig = message.getConfig();
            CustomModelClient.isServerModded = true;
            CustomModelClient.manager.clearModels();
        });
        return null;
    }
}
