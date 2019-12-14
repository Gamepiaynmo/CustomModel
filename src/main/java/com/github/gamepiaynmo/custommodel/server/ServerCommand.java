package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.command.ArgumentBuilders;
import com.github.gamepiaynmo.custommodel.client.command.Command;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerCommand {
    public static void register() {
        CommandRegistry.INSTANCE.register(false, (dispatcher) -> {
            dispatcher.register(CommandManager.literal(CustomModel.MODID
            ).then(CommandManager.literal("reload").executes(context -> {
                CustomModel.reloadModel(context.getSource().getPlayer(), true);
                return 1;
            }).then(CommandManager.argument("targets", EntityArgumentType.players()).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).executes(context -> {
                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
                players.forEach(player -> CustomModel.reloadModel(player, true));
                return players.size();
            }))).then(CommandManager.literal("list").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).executes(context -> {
                List<File> fileList = Lists.newArrayList(new File(CustomModel.MODEL_DIR).listFiles());
                context.getSource().sendFeedback(new TranslatableText("command.custommodel.listmodels",
                        fileList.size(), Texts.join(fileList, (file) -> {
                    return new LiteralText(file.getName());
                })), false);
                return fileList.size();
            })).then(CommandManager.literal("select").then(CommandManager.argument("model", new ModelArgumentType()).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).executes(context -> {
                CustomModel.selectModel(context.getSource().getPlayer(), context.getArgument("model", String.class));
                return 1;
            }).then(CommandManager.argument("targets", EntityArgumentType.players()).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).executes(context -> {
                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
                String model = context.getArgument("model", String.class);
                players.forEach(player -> CustomModel.selectModel(player, model));
                return players.size();
            })))));
        });
    }

    public static class ModelArgumentType implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            return reader.readUnquotedString();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            S source = context.getSource();
            if (source instanceof ServerCommandSource) {
                List<String> fileList = Lists.newArrayList();
                for (File file : new File(CustomModel.MODEL_DIR).listFiles())
                    fileList.add(file.getName());
                return CommandSource.suggestMatching(fileList, builder);
            } else if (source instanceof CommandSource) {
                return ((CommandSource) source).getCompletions((CommandContext<CommandSource>) context, builder);
            } else return Suggestions.empty();
        }
    }
}
