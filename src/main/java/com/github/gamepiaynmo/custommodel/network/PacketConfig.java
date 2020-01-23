package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

public class PacketConfig implements IPacket, IPacketHandler<PacketConfig> {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_config");
    private ModConfig.ServerConfig config;

    public PacketConfig() {
        this.config = new ModConfig.ServerConfig();
    }

    public PacketConfig(ModConfig.ServerConfig config) {
        this.config = config;
    }

    @Override
    public void read(PacketByteBuf var1) throws IOException {
        config.customEyeHeight = var1.readBoolean();
        config.customBoundingBox = var1.readBoolean();
        config.receiveModels = var1.readBoolean();
    }

    @Override
    public void write(PacketByteBuf var1) throws IOException {
        var1.writeBoolean(config.customEyeHeight);
        var1.writeBoolean(config.customBoundingBox);
        var1.writeBoolean(config.receiveModels);
    }

    public ModConfig.ServerConfig getConfig() {
        return config;
    }

    @Override
    public void apply(PacketConfig packet, PacketContext context) {
        CustomModelClient.serverConfig = packet.getConfig();
    }
}
