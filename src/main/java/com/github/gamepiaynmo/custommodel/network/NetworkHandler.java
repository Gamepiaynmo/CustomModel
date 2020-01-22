package com.github.gamepiaynmo.custommodel.network;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(CustomModel.MODID);

    private static int id = 0;

    public static void init() {
        CHANNEL.registerMessage(PacketQuery.Server.class, PacketQuery.class, id++, Side.SERVER);
        CHANNEL.registerMessage(PacketQuery.Client.class, PacketQuery.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PacketModel.Server.class, PacketModel.class, id++, Side.SERVER);
        CHANNEL.registerMessage(PacketModel.Client.class, PacketModel.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PacketConfig.class, PacketConfig.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PacketList.class, PacketList.class, id++, Side.SERVER);
        CHANNEL.registerMessage(PacketInfo.Client.class, PacketInfo.class, id++, Side.CLIENT);
    }
}
