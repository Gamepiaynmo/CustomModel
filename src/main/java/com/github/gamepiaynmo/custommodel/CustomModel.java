package com.github.gamepiaynmo.custommodel;

import com.github.gamepiaynmo.custommodel.command.ReloadModelCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;

public class CustomModel implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistry.INSTANCE.register(false, ReloadModelCommand::register);
    }
}
