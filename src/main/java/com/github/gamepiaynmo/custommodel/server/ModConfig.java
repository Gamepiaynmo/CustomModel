package com.github.gamepiaynmo.custommodel.server;

import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.ConfigData;
import me.sargunvohra.mcmods.autoconfig1.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1.annotation.ConfigEntry;

@Config(name = CustomModel.MODID)
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean hideNearParticles = true;

    @ConfigEntry.Gui.Tooltip
    public boolean customEyeHeight = true;

    @ConfigEntry.Gui.Tooltip
    public boolean customBoundingBox = true;

    public static ModConfig getConfig() { return AutoConfig.getConfigHolder(ModConfig.class).getConfig(); }

    public static boolean isHideNearParticles() { return getConfig().hideNearParticles; }
    public static boolean isCustomEyeHeight() { return getConfig().customEyeHeight; }
    public static boolean isCustomBoundingBox() { return getConfig().customBoundingBox; }
}
