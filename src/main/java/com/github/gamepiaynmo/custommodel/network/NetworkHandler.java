package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(CustomModel.MODID);

    public static void init() {
        CHANNEL.registerMessage(PacketQuery.class, PacketQuery.class, 0, Side.SERVER);
        CHANNEL.registerMessage(PacketModel.class, PacketModel.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(PacketReplyConfig.class, PacketReplyConfig.class, 2, Side.CLIENT);
    }
}
