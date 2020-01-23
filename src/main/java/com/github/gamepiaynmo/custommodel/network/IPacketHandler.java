package com.github.gamepiaynmo.custommodel.network;

import net.fabricmc.fabric.api.network.PacketContext;

public interface IPacketHandler<T extends IPacket > {
    void apply(T packet, PacketContext context);
}
