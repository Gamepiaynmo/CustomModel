package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModelLoadInfo;
import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class PacketInfo implements IPacket {
    public static final Identifier ID = new Identifier(CustomModel.MODID, "packet_info");
    private List<ModelPackInfo> infoList;

    @Override
    public void read(PacketByteBuf buf) throws IOException {
        int size = buf.readInt();
        infoList = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++)
            infoList.add(readInfo(buf));
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException {
        Collection<String> models = CustomModel.manager.getModelIdList();
        buf.writeInt(models.size());
        for (String id : models)
            writeInfo(CustomModel.manager.models.get(id), buf);
    }

    private void writeInfo(ModelLoadInfo info, PacketByteBuf buf) {
        buf.writeString(info.info.modelId);
        buf.writeString(info.info.modelName);
        buf.writeString(info.info.version);
        buf.writeString(info.info.author);
        buf.writeBoolean(info.isClient);
    }

    private ModelPackInfo readInfo(PacketByteBuf buf) {
        return new ModelPackInfo(buf.readString(64), buf.readString(256),
                buf.readString(64), buf.readString(256), buf.readBoolean());
    }

    public static class Client implements IPacketHandler<PacketInfo> {
        @Override
        public void apply(PacketInfo packet, PacketContext context) {
            CustomModelClient.showModelSelectionGui(packet.infoList);
        }
    }
}
