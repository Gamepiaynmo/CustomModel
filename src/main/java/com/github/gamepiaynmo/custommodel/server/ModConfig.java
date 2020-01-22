package com.github.gamepiaynmo.custommodel.server;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

@Config(modid = CustomModel.MODID, name = "CustomModel")
public class ModConfig {
    @Config.LangKey("text.autoconfig.custommodel.category.client")
    @Config.Name("ClientConfig")
    public static ClientConfig client = new ClientConfig();

    @Config.LangKey("text.autoconfig.custommodel.category.server")
    @Config.Name("ServerConfig")
    public static ServerConfig server = new ServerConfig();

    @Config.LangKey("text.autoconfig.custommodel.category.permission")
    @Config.Name("PermissionConfig")
    public static Permissions permission = new Permissions();

    public static class ClientConfig {
        @Config.Comment("Hide particles that are too close to player's eye.")
        @Config.LangKey("text.autoconfig.custommodel.option.client.hideNearParticles")
        @Config.Name("HideNearParticles")
        public boolean hideNearParticles = true;

        @Config.Comment("Automatically send the models that the server does not have.")
        @Config.LangKey("text.autoconfig.custommodel.option.client.sendModels")
        @Config.Name("SendModels")
        public boolean sendModels = true;
    }

    public static class ServerConfig {
        @Config.Comment("Allow model packs to change eye height. Only effects client side if not allowed at server side.")
        @Config.LangKey("text.autoconfig.custommodel.option.server.customEyeHeight")
        @Config.Name("CustomEyeHeight")
        public boolean customEyeHeight = true;

        @Config.Comment("Allow model packs to change bounding box. Will not take effect if server side is not modded or not allowed.")
        @Config.LangKey("text.autoconfig.custommodel.option.server.customBoundingBox")
        @Config.Name("CustomBoundingBox")
        public boolean customBoundingBox = true;

        @Config.Comment("Default model when no model has been select by the player.")
        @Config.LangKey("text.autoconfig.custommodel.option.server.defaultModel")
        @Config.Name("DefaultModel")
        public String defaultModel = "default";

        @Config.Comment("Receive models that the server does not have from clients.")
        @Config.LangKey("text.autoconfig.custommodel.option.server.receiveModels")
        @Config.Name("ReceiveModels")
        public boolean receiveModels = true;
    }

    public static class Permissions {
        @Config.Comment("Permission level needed to reload model of oneself.")
        @Config.LangKey("text.autoconfig.custommodel.option.permission.reloadSelfPermission")
        @Config.Name("ReloadSelfPermission")
        public int reloadSelfPermission = 0;

        @Config.Comment("Permission level needed to reload model of other players.")
        @Config.LangKey("text.autoconfig.custommodel.option.permission.reloadOthersPermission")
        @Config.Name("ReloadOthersPermission")
        public int reloadOthersPermission = 1;

        @Config.Comment("Permission level needed to select model of oneself.")
        @Config.LangKey("text.autoconfig.custommodel.option.permission.selectSelfPermission")
        @Config.Name("SelectSelfPermission")
        public int selectSelfPermission = 1;

        @Config.Comment("Permission level needed to select model of other players.")
        @Config.LangKey("text.autoconfig.custommodel.option.permission.selectOthersPermission")
        @Config.Name("SelectOthersPermission")
        public int selectOthersPermission = 2;

        @Config.Comment("Permission level needed to list all models.")
        @Config.LangKey("text.autoconfig.custommodel.option.permission.listModelsPermission")
        @Config.Name("ListModelsPermission")
        public int listModelsPermission = 1;
    }

    public static boolean isHideNearParticles() { return client.hideNearParticles; }
    public static boolean isSendModels() { return client.sendModels; }

    public static boolean isCustomEyeHeight() { return server.customEyeHeight; }
    public static boolean isCustomBoundingBox() { return server.customBoundingBox; }
    public static String getDefaultModel() { return server.defaultModel; }
    public static boolean isReceiveModels() { return server.receiveModels; }

    public static int getReloadSelfPermission() { return permission.reloadSelfPermission; }
    public static int getReloadOthersPermission() { return permission.reloadOthersPermission; }
    public static int getSelectSelfPermission() { return permission.selectSelfPermission; }
    public static int getSelectOthersPermission() { return permission.selectOthersPermission; }
    public static int getListModelsPermission() { return permission.listModelsPermission; }
}
