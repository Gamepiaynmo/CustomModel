package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class PacketQueryConfig implements IMessage, IMessageHandler<PacketQueryConfig, PacketReplyConfig> {

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    @Override
    public PacketReplyConfig onMessage(PacketQueryConfig message, MessageContext ctx) {
        return new PacketReplyConfig(ModConfig.getSettings().server);
    }
}
