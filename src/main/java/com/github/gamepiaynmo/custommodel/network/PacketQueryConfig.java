package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

public class PacketQueryConfig implements Packet<ServerPlayPacketListener> {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_query_config");

    @Override
    public void read(PacketByteBuf var1) throws IOException {

    }

    @Override
    public void write(PacketByteBuf var1) throws IOException {

    }

    @Override
    public void apply(ServerPlayPacketListener var1) {

    }
}
