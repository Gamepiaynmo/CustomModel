package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;
import java.util.UUID;

public class PacketQuery implements IPacket {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_query");
    private UUID playerUuid = new UUID(0, 0);
    private String modelId = "";

    public PacketQuery(UUID uuid) {
        playerUuid = uuid;
    }

    public PacketQuery(UUID uuid, String id) {
        this(uuid);
        modelId = id;
    }

    public PacketQuery() {}

    @Override
    public void read(PacketByteBuf buf) throws IOException {
        playerUuid = buf.readUuid();
        modelId = buf.readString(64);
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException {
        buf.writeUuid(playerUuid);
        buf.writeString(modelId);
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }
    public String getModelId() { return modelId; }

    public static class Server implements IPacketHandler<PacketQuery> {
        @Override
        public void apply(PacketQuery packet, PacketContext context) {
            try {
                CustomModel.manager.reloadModel(context.getPlayer(), packet.getPlayerUuid(), false);
            } catch (LoadModelException e) {
                CustomModel.LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    public static class Client implements IPacketHandler<PacketQuery> {
        @Override
        public void apply(PacketQuery packet, PacketContext context) {
            try {
                CustomModelClient.manager.loadModel(packet.getPlayerUuid(), packet.getModelId());
            } catch (ModelNotFoundException e) {
                CustomModelClient.LOGGER.warn(e.getMessage(), e);
            }
        }
    }
}
