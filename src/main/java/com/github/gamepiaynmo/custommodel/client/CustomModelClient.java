package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.api.ModelPackInfo;
import com.github.gamepiaynmo.custommodel.client.gui.GuiModelSelection;
import com.github.gamepiaynmo.custommodel.entity.CustomModelFemaleNpc;
import com.github.gamepiaynmo.custommodel.entity.CustomModelMaleNpc;
import com.github.gamepiaynmo.custommodel.entity.NpcHelper;
import com.github.gamepiaynmo.custommodel.mixin.RenderPlayerHandler;
import com.github.gamepiaynmo.custommodel.client.render.*;
import com.github.gamepiaynmo.custommodel.network.NetworkHandler;
import com.github.gamepiaynmo.custommodel.network.PacketList;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.List;

public class CustomModelClient {
    public static final ClientModelManager manager = new ClientModelManager();

    public static ModConfig.ServerConfig serverConfig;
    public static boolean isServerModded = false;

    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean isRenderingInventory;
    public static EntityParameter inventoryEntityParameter;
    public static boolean isRenderingFirstPerson;

    public static KeyBinding selectModelKey = new KeyBinding("key.custommodel.selectmodel",
            KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Keyboard.KEY_M, "key.categories.misc");

    private static float lastPartial;
    public static float getPartial() {
        return Minecraft.getMinecraft().isGamePaused() ? lastPartial : (lastPartial = Minecraft.getMinecraft().getRenderPartialTicks());
    }

    private static void initServerStatus() {
        serverConfig = new ModConfig.ServerConfig();
        serverConfig.customEyeHeight = ModConfig.isCustomEyeHeight();
        serverConfig.customBoundingBox = false;
        serverConfig.receiveModels = false;
        isServerModded = false;
    }

    public static void onInitializeClient() {
        new File(CustomModel.MODEL_DIR).mkdirs();
        initServerStatus();

        MinecraftForge.EVENT_BUS.register(CustomModelClient.class);
        MinecraftForge.EVENT_BUS.register(RenderPlayerHandler.class);
        ClientRegistry.registerKeyBinding(selectModelKey);
    }

    public static void initPlayerRenderer() {
        for (RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values())
            RenderPlayerHandler.customize(renderer);
        if (CustomModel.hasnpc) {
            RenderingRegistry.registerEntityRenderingHandler(CustomModelMaleNpc.class, manager ->
                    new RenderNpc(manager, new ModelPlayer(0, false), false));
            RenderingRegistry.registerEntityRenderingHandler(CustomModelFemaleNpc.class, manager ->
                    new RenderNpc(manager, new ModelPlayer(0, true), true));
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ClientTickEvent event) {
        WorldClient world = Minecraft.getMinecraft().world;
        if (world != null && !Minecraft.getMinecraft().isGamePaused() && event.phase == TickEvent.Phase.END) {
            for (AbstractClientPlayer player : world.getPlayers(AbstractClientPlayer.class, player -> true)) {
                RenderPlayerHandler.tick(player);
            }

            if (CustomModel.hasnpc) {
                for (EntityLivingBase entity : NpcHelper.getCustomModelNpcs(world)) {
                    RenderPlayerHandler.tick(entity);
                }
            }

            manager.tick();
            if (selectModelKey.isPressed()) {
                if (isServerModded)
                    NetworkHandler.CHANNEL.sendToServer(new PacketList());
                else Minecraft.getMinecraft().displayGuiScreen(new GuiModelSelection());
            }
        }
    }

    public static void showModelSelectionGui(List<ModelPackInfo> infoList) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiModelSelection(infoList));
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        initServerStatus();
        manager.clearModels();
    }
}
