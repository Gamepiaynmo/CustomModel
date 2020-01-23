package com.github.gamepiaynmo.custommodel.network;

import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

public interface IPacket {
    void read(PacketByteBuf packetByteBuf) throws IOException;

    void write(PacketByteBuf packetByteBuf) throws IOException;
}
