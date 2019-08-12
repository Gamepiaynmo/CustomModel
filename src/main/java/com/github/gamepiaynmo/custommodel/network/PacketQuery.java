package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;
import java.util.UUID;

public class PacketQuery implements Packet<ServerPlayPacketListener> {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_query");
    private UUID playerUuid;

    public PacketQuery(GameProfile profile) {
        playerUuid = PlayerEntity.getUuidFromProfile(profile);
    }

    public PacketQuery() {}

    @Override
    public void read(PacketByteBuf buf) throws IOException {
        playerUuid = buf.readUuid();
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException {
        buf.writeUuid(playerUuid);
    }

    @Override
    public void apply(ServerPlayPacketListener var1) {

    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }
}
