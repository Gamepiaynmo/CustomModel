package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PacketModel implements Packet<ClientPlayPacketListener> {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_model");
    private String name;
    private byte[] data;

    public PacketModel(File modelFile) {
        try {
            name = modelFile.getName();
            InputStream stream = new FileInputStream(modelFile);
            int len = stream.available();
            data = new byte[len];
            stream.read(data);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PacketModel() {}

    @Override
    public void read(PacketByteBuf buf) throws IOException {
        name = buf.readString();
        int len = buf.readInt();
        data = new byte[len];
        buf.readBytes(data);
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException {
        buf.writeString(name);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void apply(ClientPlayPacketListener var1) {

    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }
}
