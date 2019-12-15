package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.*;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PacketModel implements Packet<ClientPlayPacketListener> {
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

    public PacketModel() {}

    @Override
    public void read(PacketByteBuf buf) throws IOException {
        uuid = buf.readUuid();
        int len = buf.readInt();
        data = new byte[len];
        buf.readBytes(data);
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException {
        buf.writeUuid(uuid);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void apply(ClientPlayPacketListener var1) {

    }

    public UUID getUuid() {
        return uuid;
    }

    public byte[] getData() {
        return data;
    }
}
