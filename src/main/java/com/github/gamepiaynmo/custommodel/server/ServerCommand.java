package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.command.ArgumentBuilders;
import com.github.gamepiaynmo.custommodel.client.command.Command;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class ServerCommand {
    public static void register() {
        CommandRegistry.INSTANCE.register(false, (dispatcher) -> {
            dispatcher.register(CommandManager.literal(CustomModel.MODID)
                    .then(CommandManager.literal("reload").then(CommandManager.argument("targets", EntityArgumentType.players()).executes(context -> {
                        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");

                        return players.size();
                    }))).then(CommandManager.literal("list").executes(context -> {
                        List<File> fileList = Lists.newArrayList(new File(CustomModel.MODEL_DIR).listFiles());
                        context.getSource().sendFeedback(Texts.join(fileList, (file) -> {
                            return new LiteralText(file.getName());
                        }), false);
                        return fileList.size();
                    })));
        });
    }
}
