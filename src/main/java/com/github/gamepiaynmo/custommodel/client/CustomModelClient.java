package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import com.github.gamepiaynmo.custommodel.client.gui.GuiModelSelection;
import com.github.gamepiaynmo.custommodel.network.*;
import com.github.gamepiaynmo.custommodel.client.render.*;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class CustomModelClient implements ClientModInitializer {
    public static final ClientModelManager manager = new ClientModelManager();

    public static TextureManager textureManager;
    public static Map<String, PlayerEntityRenderer> playerRenderers;

    public static ModConfig.ServerConfig serverConfig;

    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean isRenderingInventory;
    public static EntityParameter inventoryEntityParameter;
    public static boolean isRenderingFirstPerson;

    public static FabricKeyBinding selectModelKey = FabricKeyBinding.Builder.create(
            new Identifier(CustomModel.MODID, "selectmodel"),
            InputUtil.Type.KEYSYM,
            77, "key.categories.misc"
    ).build();

    public static boolean isServerModded() {
        return ClientSidePacketRegistry.INSTANCE.canServerReceive(PacketModel.ID);
    }

    public static void sendPacket(Identifier id, IPacket packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
            ClientSidePacketRegistry.INSTANCE.sendToServer(id, buf);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public static <T extends IPacket> void registerPacket(Identifier id, Class<T> packetClass, Class<? extends IPacketHandler<T>> handlerClass) {
        ClientSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> {
            try {
                IPacket packet = packetClass.newInstance();
                packet.read(buffer);
                context.getTaskQueue().execute(() -> {
                    try {
                        IPacketHandler handler = handlerClass.newInstance();
                        handler.apply(packet, context);
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });
    }

    public static float getPartial() {
        return ((IPartial) (Object) MinecraftClient.getInstance()).getPartial();
    }

    public static void initServerStatus() {
        serverConfig = new ModConfig.ServerConfig();
        serverConfig.customEyeHeight = ModConfig.isCustomEyeHeight();
        serverConfig.customBoundingBox = false;
        serverConfig.receiveModels = false;
    }

    @Override
    public void onInitializeClient() {
        new File(CustomModel.MODEL_DIR).mkdirs();

        WorldTickCallback.EVENT.register(world -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (world == client.world) {
                for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                    PlayerEntityRenderer renderer = client.getEntityRenderManager().getRenderer(player);
                    ((ICustomPlayerRenderer) renderer).tick(player);
                }
            }

            if (selectModelKey.isPressed() && client.currentScreen == null) {
                if (CustomModelClient.isServerModded()) {
                    CustomModelClient.sendPacket(PacketList.ID, new PacketList());
                } else client.openScreen(new GuiModelSelection());
            }
        });

        registerPacket(PacketConfig.ID, PacketConfig.class, PacketConfig.class);
        registerPacket(PacketModel.ID, PacketModel.class, PacketModel.Client.class);
        registerPacket(PacketQuery.ID, PacketQuery.class, PacketQuery.Client.class);
        registerPacket(PacketInfo.ID, PacketInfo.class, PacketInfo.Client.class);

        initServerStatus();
        KeyBindingRegistry.INSTANCE.register(selectModelKey);
    }

    public static void showModelSelectionGui(List<ModelPackInfo> infoList) {
        MinecraftClient.getInstance().openScreen(new GuiModelSelection(infoList));
    }
}
