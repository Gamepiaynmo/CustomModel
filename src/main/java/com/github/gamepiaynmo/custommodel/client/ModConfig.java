package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.ConfigData;
import me.sargunvohra.mcmods.autoconfig1.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1.annotation.ConfigEntry;

@Config(name = CustomModel.MODID)
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    boolean clientFirst = false;

    private static ModConfig getConfig() { return AutoConfig.getConfigHolder(ModConfig.class).getConfig(); }

    public static boolean isClientFirst() { return getConfig().clientFirst; }
}
