package com.github.gamepiaynmo.custommodel.server;

import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.ConfigData;
import me.sargunvohra.mcmods.autoconfig1.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1.annotation.ConfigEntry;

@Config(name = CustomModel.MODID)
public class ModConfig implements ConfigData {

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    public ClientConfig client = new ClientConfig();

    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.TransitiveObject
    public ServerConfig server = new ServerConfig();

    @ConfigEntry.Category("permission")
    @ConfigEntry.Gui.TransitiveObject
    public Permissions permission = new Permissions();

    @Config(name = "client")
    public static class ClientConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public boolean hideNearParticles = true;
    }

    @Config(name = "server")
    public static class ServerConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public boolean customEyeHeight = true;

        @ConfigEntry.Gui.Tooltip
        public boolean customBoundingBox = true;
    }

    @Config(name = "permission")
    public static class Permissions implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public int reloadSelfPermission = 0;

        @ConfigEntry.Gui.Tooltip
        public int reloadOthersPermission = 1;

        @ConfigEntry.Gui.Tooltip
        public int selectSelfPermission = 1;

        @ConfigEntry.Gui.Tooltip
        public int selectOthersPermission = 2;

        @ConfigEntry.Gui.Tooltip
        public int listModelsPermission = 1;
    }

    public static ModConfig getConfig() { return AutoConfig.getConfigHolder(ModConfig.class).getConfig(); }

    public static boolean isHideNearParticles() { return getConfig().client.hideNearParticles; }

    public static boolean isCustomEyeHeight() { return getConfig().server.customEyeHeight; }
    public static boolean isCustomBoundingBox() { return getConfig().server.customBoundingBox; }

    public static int getReloadSelfPermission() { return getConfig().permission.reloadSelfPermission; }
    public static int getReloadOthersPermission() { return getConfig().permission.reloadOthersPermission; }
    public static int getSelectSelfPermission() { return getConfig().permission.selectSelfPermission; }
    public static int getSelectOthersPermission() { return getConfig().permission.selectOthersPermission; }
    public static int getListModelsPermission() { return getConfig().permission.listModelsPermission; }
}
