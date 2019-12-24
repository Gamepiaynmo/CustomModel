package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.UUID;

public class PacketQuery implements IMessage, IMessageHandler<PacketQuery, PacketModel> {
    private UUID playerUuid;

    public PacketQuery(GameProfile profile) {
        playerUuid = EntityPlayer.getUUID(profile);
    }

    public PacketQuery() {}

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerUuid = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(playerUuid.getMostSignificantBits());
        buf.writeLong(playerUuid.getLeastSignificantBits());
    }

    @Override
    public PacketModel onMessage(PacketQuery message, MessageContext ctx) {
        ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
            try {
                CustomModel.reloadModel(ctx.getServerHandler().player, message.getPlayerUuid(), false);
            } catch (LoadModelException e) {
            }
        });
        return null;
    }
}
