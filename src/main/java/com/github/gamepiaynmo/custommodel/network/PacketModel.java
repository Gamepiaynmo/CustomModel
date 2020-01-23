package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.*;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PacketModel implements IPacket {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_model");
    private UUID uuid;
    private byte[] data;
    public boolean success;

    public PacketModel(File modelFile, UUID name) {
        try {
            this.uuid = name;
            if (modelFile.isFile()) {
                InputStream stream = new FileInputStream(modelFile);
                int len = stream.available();
                data = new byte[len];
                stream.read(data);
                stream.close();
            } else {
                ByteArrayOutputStream array = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(array);
                for (File file : modelFile.listFiles()) {
                    ZipEntry entry = new ZipEntry(file.getName());
                    zip.putNextEntry(entry);

                    FileInputStream fin = new FileInputStream(file);
                    byte[] buffer = new byte[fin.available()];
                    fin.read(buffer, 0, buffer.length);
                    fin.close();
                    zip.write(buffer);
                    zip.closeEntry();
                }

                zip.flush();
                zip.close();
                array.flush();
                array.close();
                data = array.toByteArray();
            }
            success = true;
        } catch (IOException e) {
            CustomModel.LOGGER.warn(e.getMessage(), e);
            success = false;
        }
    }

    public PacketModel(UUID name) {
        this(name, new byte[0]);
    }

    public PacketModel(UUID name, byte[] data) {
        this.uuid = name;
        this.data = data;
        success = true;
    }

    public PacketModel() {}

    @Override
    public void read(PacketByteBuf buf) throws IOException {
        uuid = buf.readUuid();
        int len = buf.readInt();
        data = new byte[len];
        if (len > 0)
            buf.readBytes(data);
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException {
        buf.writeUuid(uuid);
        buf.writeInt(data.length);
        if (data.length > 0)
            buf.writeBytes(data);
    }

    public UUID getUuid() {
        return uuid;
    }

    public byte[] getData() {
        return data;
    }

    public static class Server implements IPacketHandler<PacketModel> {
        @Override
        public void apply(PacketModel packet, PacketContext context) {
            try {
                ModelInfo info = ModelInfo.fromZipMemory(packet.getData());
                info.sender = context.getPlayer().getUuid();
                CustomModel.manager.addModelInfo(info, (ServerPlayerEntity) context.getPlayer(), packet.getUuid());
            } catch (Exception e) {
                CustomModel.LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    public static class Client implements IPacketHandler<PacketModel> {
        @Override
        public void apply(PacketModel packet, PacketContext context) {
            ModelPack pack = null;
            try {
                if (packet.getData().length > 0) {
                    pack = ModelPack.fromZipMemory(CustomModelClient.textureManager, packet.getUuid(), packet.getData());
                    if (pack != null && pack.successfulLoaded())
                        CustomModelClient.manager.addModel(packet.getUuid(), pack);
                } else {
                    CustomModelClient.manager.clearModel(packet.getUuid());
                }
            } catch (Exception e) {
                MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.CHAT,
                        new TranslatableText("error.custommodel.loadmodelpack", "", e.getMessage()).formatted(Formatting.RED));
                CustomModelClient.LOGGER.warn(e.getMessage(), e);
            }
        }
    }
}
