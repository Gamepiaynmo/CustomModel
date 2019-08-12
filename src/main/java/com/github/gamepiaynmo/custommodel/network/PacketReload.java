package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PacketReload implements Packet<ClientPlayPacketListener> {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_reload");
    private List<UUID> uuids = Lists.newArrayList();

    public PacketReload(Collection<ServerPlayerEntity> player) {
        player.forEach(playerEntity -> {
            GameProfile profile = playerEntity.getGameProfile();
            uuids.add(PlayerEntity.getUuidFromProfile(profile));
        });
    }

    public PacketReload() {}

    @Override
    public void read(PacketByteBuf buf) throws IOException {
        int size = buf.readByte();
        while (size-- > 0)
            uuids.add(buf.readUuid());
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException {
        buf.writeByte(uuids.size());
        for (UUID uuid : uuids)
            buf.writeUuid(uuid);
    }

    @Override
    public void apply(ClientPlayPacketListener var1) {

    }

    public Collection<UUID> getUUIDs() {
        return uuids;
    }
}
