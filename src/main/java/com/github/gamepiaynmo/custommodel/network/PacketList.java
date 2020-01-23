package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

public class PacketList implements IPacket, IPacketHandler<PacketList> {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_list");

    @Override
    public void read(PacketByteBuf packetByteBuf) throws IOException {

    }

    @Override
    public void write(PacketByteBuf packetByteBuf) throws IOException {

    }

    @Override
    public void apply(PacketList packet, PacketContext context) {
        CustomModel.sendPacket(context.getPlayer(), PacketInfo.ID, new PacketInfo());
    }
}
