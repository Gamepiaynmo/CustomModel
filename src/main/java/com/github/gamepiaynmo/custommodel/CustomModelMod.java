package com.github.gamepiaynmo.custommodel;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.mixin.asm.TransformGuiInventory;
import com.github.gamepiaynmo.custommodel.mixin.asm.TransformRenderPlayer;
import com.github.gamepiaynmo.custommodel.network.NetworkHandler;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.ServerCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import squeek.asmhelper.ObfHelper;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(1000)
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions({"com.github.gamepiaynmo.custommodel"})
@Mod(modid = CustomModel.MODID, useMetadata = true, guiFactory = "com.github.gamepiaynmo.custommodel.client.ModMenu")
public class CustomModelMod implements IFMLLoadingPlugin {
    public static Logger LOGGER;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        CustomModel.onInitialize();

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
            CustomModelClient.onInitializeClient();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkHandler.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        CustomModel.server = event.getServer();
        ((CommandHandler) CustomModel.server.commandManager).registerCommand(new ServerCommand());
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
        CustomModel.server = null;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                TransformRenderPlayer.class.getName(),
                TransformGuiInventory.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        ObfHelper.setObfuscated((Boolean) data.get("runtimeDeobfuscationEnabled"));
        ObfHelper.setRunsAfterDeobfRemapper(true);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
