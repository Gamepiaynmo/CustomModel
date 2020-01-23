package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.entity.CustomModelFemaleNpc;
import com.github.gamepiaynmo.custommodel.entity.CustomModelMaleNpc;
import com.github.gamepiaynmo.custommodel.entity.NpcHelper;
import com.github.gamepiaynmo.custommodel.mixin.PlayerStatureHandler;
import com.github.gamepiaynmo.custommodel.network.*;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class CustomModel {
    public static final String MODID = "custommodel";
    public static final String MODEL_DIR = "custom-models";

    public static final Logger LOGGER = LogManager.getLogger();

    public static MinecraftServer server;
    public static final ServerModelManager manager = new ServerModelManager();

    public static boolean hasnpc;

    public static void onInitialize() {
        hasnpc = Loader.isModLoaded("customnpcs");

        new File(MODEL_DIR).mkdirs();
        manager.refreshModelList();

        MinecraftForge.EVENT_BUS.register(CustomModel.class);
        MinecraftForge.EVENT_BUS.register(PlayerStatureHandler.class);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (hasnpc) {
                for (WorldServer worldServer : server.worlds)
                    NpcHelper.updateCustomModelNpcs(worldServer);
            }
            manager.tick();
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        server.addScheduledTask(() -> {
            NetworkHandler.CHANNEL.sendTo(new PacketConfig(), (EntityPlayerMP) event.player);
        });
    }

    @SubscribeEvent
    public static void onPlayerExit(PlayerEvent.PlayerLoggedOutEvent event) {
        manager.onPlayerExit((EntityPlayerMP) event.player);
    }

    public static void onServerStart(MinecraftServer server) {
        CustomModel.server = server;
        ((CommandHandler) server.commandManager).registerCommand(new ServerCommand());
    }

    public static void onServerStop() {
        CustomModel.server = null;
    }

    @SubscribeEvent
    public static void onRegisterEntity(RegistryEvent.Register<EntityEntry> event) {
        if (hasnpc) {
            event.getRegistry().registerAll(
                    EntityEntryBuilder.create()
                            .entity(CustomModelMaleNpc.class)
                            .id(new ResourceLocation(MODID, "custommodel.male"), 0)
                            .name("CPM Male")
                            .tracker(64, 3, true)
                            .build(),

                    EntityEntryBuilder.create()
                            .entity(CustomModelFemaleNpc.class)
                            .id(new ResourceLocation(MODID, "custommodel.female"), 0)
                            .name("CPM Female")
                            .tracker(64, 3, true)
                            .build()
            );
        }
    }

    @SubscribeEvent
    public static void onConfigUpdated(ConfigChangedEvent.PostConfigChangedEvent event) {
        ConfigManager.sync(CustomModel.MODID, Config.Type.INSTANCE);
    }
}
