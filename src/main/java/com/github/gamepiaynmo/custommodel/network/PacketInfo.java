package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModelInfo;
import com.github.gamepiaynmo.custommodel.server.ModelLoadInfo;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collection;
import java.util.List;

public class PacketInfo implements IMessage {
    private List<ModelPackInfo> infoList;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        infoList = Lists.newArrayListWithCapacity(size);
        PacketBuffer packetBuf = new PacketBuffer(buf);
        for (int i = 0; i < size; i++)
            infoList.add(readInfo(packetBuf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        Collection<String> models = CustomModel.manager.getModelIdList();
        buf.writeInt(models.size());
        PacketBuffer packetBuf = new PacketBuffer(buf);
        for (String id : models)
            writeInfo(CustomModel.manager.models.get(id), packetBuf);
    }

    private void writeInfo(ModelLoadInfo info, PacketBuffer buf) {
        buf.writeString(info.info.modelId);
        buf.writeString(info.info.modelName);
        buf.writeString(info.info.version);
        buf.writeString(info.info.author);
        buf.writeBoolean(info.isClient);
    }

    private ModelPackInfo readInfo(PacketBuffer buf) {
        return new ModelPackInfo(buf.readString(64), buf.readString(256),
                buf.readString(64), buf.readString(256), buf.readBoolean());
    }

    public static class Client implements IMessageHandler<PacketInfo, IMessage> {
        @Override
        public IMessage onMessage(PacketInfo message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                CustomModelClient.showModelSelectionGui(message.infoList);
            });
            return null;
        }
    }
}
