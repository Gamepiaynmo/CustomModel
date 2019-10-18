package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class ModMenu implements ModMenuApi {
    @Override
    public String getModId() {
        return CustomModel.MODID;
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return screen -> AutoConfig.getConfigScreen(ModConfig.class, screen).get();
    }
}
