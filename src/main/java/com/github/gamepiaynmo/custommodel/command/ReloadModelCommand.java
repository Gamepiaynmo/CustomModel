package com.github.gamepiaynmo.custommodel.command;

import com.github.gamepiaynmo.custommodel.CustomModelClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.Collection;
import java.util.Iterator;

public class ReloadModelCommand {
    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder) CommandManager.literal("reloadmodels")).executes((commandContext) -> {
            return execute((ServerCommandSource)commandContext.getSource());
        }));
    }

    private static int execute(ServerCommandSource serverCommandSource) {
        CustomModelClient.reloadModels();
        return 1;
    }
}
