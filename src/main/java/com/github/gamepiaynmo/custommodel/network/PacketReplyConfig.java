package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

public class PacketReplyConfig implements Packet<ClientPlayPacketListener> {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_reply_config");
    private ModConfig.ServerConfig config;

    public PacketReplyConfig() {
        this.config = new ModConfig.ServerConfig();
    }

    public PacketReplyConfig(ModConfig.ServerConfig config) {
        this.config = config;
    }

    @Override
    public void read(PacketByteBuf var1) throws IOException {
        config.customEyeHeight = var1.readBoolean();
        config.customBoundingBox = var1.readBoolean();
    }

    @Override
    public void write(PacketByteBuf var1) throws IOException {
        var1.writeBoolean(config.customEyeHeight);
        var1.writeBoolean(config.customBoundingBox);
    }

    @Override
    public void apply(ClientPlayPacketListener var1) {

    }

    public ModConfig.ServerConfig getConfig() {
        return config;
    }
}
