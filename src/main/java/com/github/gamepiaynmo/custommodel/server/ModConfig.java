package com.github.gamepiaynmo.custommodel.server;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ModConfig {
    static Configuration CONFIG = new Configuration();
    static ModConfig SETTINGS = new ModConfig();

    public ClientConfig client = new ClientConfig();
    public ServerConfig server = new ServerConfig();
    public Permissions permission = new Permissions();

    public static class ClientConfig {
        public boolean hideNearParticles = true;
    }

    public static class ServerConfig {
        public boolean customEyeHeight = true;
        public boolean customBoundingBox = true;
        public String defaultModel = "default";
    }

    public static class Permissions {
        public int reloadSelfPermission = 0;
        public int reloadOthersPermission = 1;
        public int selectSelfPermission = 1;
        public int selectOthersPermission = 2;
        public int listModelsPermission = 1;
    }

    public static ModConfig getSettings() { return SETTINGS; }
    public static void updateConfig() {
        SETTINGS.client.hideNearParticles = CONFIG.getBoolean("hideNearParticles", "client", true, getTranslated("text.autoconfig.custommodel.option.client.hideNearParticles.@Tooltip"), "text.autoconfig.custommodel.option.client.hideNearParticles");

        SETTINGS.server.customEyeHeight = CONFIG.getBoolean("customEyeHeight", "server", true, getTranslated("text.autoconfig.custommodel.option.server.customEyeHeight.@Tooltip"), "text.autoconfig.custommodel.option.server.customEyeHeight");
        SETTINGS.server.customBoundingBox = CONFIG.getBoolean("customBoundingBox", "server", true, getTranslated("text.autoconfig.custommodel.option.server.customBoundingBox.@Tooltip"), "text.autoconfig.custommodel.option.server.customBoundingBox");
        SETTINGS.server.defaultModel = CONFIG.getString("defaultModel", "server", "default", getTranslated("text.autoconfig.custommodel.option.server.defaultModel.@Tooltip"), "text.autoconfig.custommodel.option.server.defaultModel");

        SETTINGS.permission.reloadSelfPermission = CONFIG.getInt("reloadSelfPermission", "permission", 0, 0, 4, getTranslated("text.autoconfig.custommodel.option.permission.reloadSelfPermission.@Tooltip"), "text.autoconfig.custommodel.option.permission.reloadSelfPermission");
        SETTINGS.permission.reloadOthersPermission = CONFIG.getInt("reloadOthersPermission", "permission", 1, 0, 4, getTranslated("text.autoconfig.custommodel.option.permission.reloadOthersPermission.@Tooltip"), "text.autoconfig.custommodel.option.permission.reloadOthersPermission");
        SETTINGS.permission.selectSelfPermission = CONFIG.getInt("selectSelfPermission", "permission", 1, 0, 4, getTranslated("text.autoconfig.custommodel.option.permission.selectSelfPermission.@Tooltip"), "text.autoconfig.custommodel.option.permission.selectSelfPermission");
        SETTINGS.permission.selectOthersPermission = CONFIG.getInt("selectOthersPermission", "permission", 2, 0, 4, getTranslated("text.autoconfig.custommodel.option.permission.selectOthersPermission.@Tooltip"), "text.autoconfig.custommodel.option.permission.selectOthersPermission");
        SETTINGS.permission.listModelsPermission = CONFIG.getInt("listModelsPermission", "permission", 1, 0, 4, getTranslated("text.autoconfig.custommodel.option.permission.listModelsPermission.@Tooltip"), "text.autoconfig.custommodel.option.permission.listModelsPermission");
    }

    private static String getTranslated(String key) {
        return I18n.translateToLocal(key);
    }

    public static boolean isHideNearParticles() { return getSettings().client.hideNearParticles; }

    public static boolean isCustomEyeHeight() { return getSettings().server.customEyeHeight; }
    public static boolean isCustomBoundingBox() { return getSettings().server.customBoundingBox; }
    public static String getDefaultModel() { return getSettings().server.defaultModel; }

    public static int getReloadSelfPermission() { return getSettings().permission.reloadSelfPermission; }
    public static int getReloadOthersPermission() { return getSettings().permission.reloadOthersPermission; }
    public static int getSelectSelfPermission() { return getSettings().permission.selectSelfPermission; }
    public static int getSelectOthersPermission() { return getSettings().permission.selectOthersPermission; }
    public static int getListModelsPermission() { return getSettings().permission.listModelsPermission; }
}
