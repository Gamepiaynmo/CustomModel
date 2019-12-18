package com.github.gamepiaynmo.custommodel.server;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.command.ArgumentBuilders;
import com.github.gamepiaynmo.custommodel.client.command.Command;
import com.github.gamepiaynmo.custommodel.util.LoadModelException;
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
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerCommand {
    public static void register() {
        CommandRegistry.INSTANCE.register(false, (dispatcher) -> {
            dispatcher.register(CommandManager.literal(CustomModel.MODID
            ).then(CommandManager.literal("reload").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(ModConfig.getReloadSelfPermission())).executes(context -> {
                try {
                    CustomModel.reloadModel(context.getSource().getPlayer(), true);
                } catch (LoadModelException e) {
                    context.getSource().sendFeedback(new TranslatableText("error.custommodel.loadmodelpack", e.getFileName(), e.getMessage()).formatted(Formatting.RED), false);
                }
                context.getSource().sendFeedback(new TranslatableText("command.custommodel.reload", 1), true);
                return 1;
            }).then(CommandManager.argument("targets", EntityArgumentType.players()).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(ModConfig.getReloadOthersPermission())).executes(context -> {
                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
                try {
                    for (ServerPlayerEntity player : players)
                        CustomModel.reloadModel(player, true);
                } catch (LoadModelException e) {
                    context.getSource().sendFeedback(new TranslatableText("error.custommodel.loadmodelpack", e.getFileName(), e.getMessage()).formatted(Formatting.RED), false);
                }
                context.getSource().sendFeedback(new TranslatableText("command.custommodel.reload", players.size()), true);
                return players.size();
            }))).then(CommandManager.literal("list").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(ModConfig.getListModelsPermission())).executes(context -> {
                Collection<Text> info = CustomModel.getModelInfoList();
                context.getSource().sendFeedback(new TranslatableText("command.custommodel.listmodels", info.size()), false);
                for (Text str : info)
                    context.getSource().sendFeedback(str, false);
                return info.size();
            })).then(CommandManager.literal("select").then(CommandManager.argument("model", new ModelArgumentType()).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(ModConfig.getSelectSelfPermission())).executes(context -> {
                String model = context.getArgument("model", String.class);
                try {
                    CustomModel.selectModel(context.getSource().getPlayer(), model);
                } catch (LoadModelException e) {
                    context.getSource().sendFeedback(new TranslatableText("error.custommodel.loadmodelpack", e.getFileName(), e.getMessage()).formatted(Formatting.RED), false);
                }
                context.getSource().sendFeedback(new TranslatableText("command.custommodel.select", 1, model), true);
                return 1;
            }).then(CommandManager.argument("targets", EntityArgumentType.players()).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(ModConfig.getSelectOthersPermission())).executes(context -> {
                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "targets");
                String model = context.getArgument("model", String.class);
                try {
                    for (ServerPlayerEntity player : players)
                        CustomModel.selectModel(player, model);
                } catch (LoadModelException e) {
                    context.getSource().sendFeedback(new TranslatableText("error.custommodel.loadmodelpack", e.getFileName(), e.getMessage()).formatted(Formatting.RED), false);
                }
                context.getSource().sendFeedback(new TranslatableText("command.custommodel.select", players.size(), model), true);
                return players.size();
            })))).then(CommandManager.literal("refresh").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(ModConfig.getListModelsPermission())).executes(context -> {
                CustomModel.refreshModelList();
                context.getSource().sendFeedback(new TranslatableText("command.custommodel.listmodels", CustomModel.models.size()), false);
                return 1;
            })));
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
                return CommandSource.suggestMatching(CustomModel.getModelIdList(), builder);
            } else if (source instanceof CommandSource) {
                return ((CommandSource) source).getCompletions((CommandContext<CommandSource>) context, builder);
            } else return Suggestions.empty();
        }
    }
}
