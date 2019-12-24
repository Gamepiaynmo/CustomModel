package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.Packet;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.*;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PacketModel implements IMessage, IMessageHandler<PacketModel, IMessage> {
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

    public PacketModel() {}

    public UUID getUuid() {
        return uuid;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = new UUID(buf.readLong(), buf.readLong());
        int len = buf.readInt();
        data = new byte[len];
        buf.readBytes(data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public IMessage onMessage(PacketModel message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ModelPack pack = null;
            try {
                TextureManager textureManager = Minecraft.getMinecraft().renderEngine;
                pack = ModelPack.fromZipMemory(textureManager, message.getUuid(), message.getData());
            } catch (Exception e) {
                TextComponentTranslation text = new TextComponentTranslation("error.custommodel.loadmodelpack", "", e.getMessage());
                text.getStyle().setColor(TextFormatting.RED);
                Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.CHAT, text);
                CustomModelClient.LOGGER.warn(e.getMessage(), e);
            }
            if (pack != null && pack.successfulLoaded())
                CustomModelClient.addModel(message.getUuid(), pack);
        });
        return null;
    }
}
