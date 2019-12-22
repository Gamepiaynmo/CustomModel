package com.github.gamepiaynmo.custommodel.client;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.util.ModelNotFoundException;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Command implements ClientCommandPlugin {
    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
        dispatcher.register(ArgumentBuilders.literal(CustomModel.MODID
        ).then(ArgumentBuilders.literal("reload").executes(context -> {
            CustomModelClient.reloadModel(MinecraftClient.getInstance().player.getGameProfile());
            context.getSource().sendFeedback(new TranslatableText("command.custommodel.reload", 1));
            return 1;
        }).then(ArgumentBuilders.argument("targets", EntityArgumentType.players()).executes(context -> {
            List<GameProfile> players = ((IClientEntitySelector) (Object) context.getArgument("targets", EntitySelector.class)).getPlayers(context.getSource());
            for (GameProfile player : players)
                CustomModelClient.reloadModel(player);
            context.getSource().sendFeedback(new TranslatableText("command.custommodel.reload", players.size()));
            return players.size();
        }))).then(ArgumentBuilders.literal("list").executes(context -> {
            Collection<Text> info = CustomModel.getModelInfoList();
            context.getSource().sendFeedback(new TranslatableText("command.custommodel.listmodels", info.size()));
            for (Text str : info)
                context.getSource().sendFeedback(str);
            return info.size();
        })).then(ArgumentBuilders.literal("select").then(ArgumentBuilders.argument("model", new ModelArgumentType()).executes(context -> {
            String model = context.getArgument("model", String.class);
            CustomModelClient.selectModel(MinecraftClient.getInstance().player.getGameProfile(), model);
            context.getSource().sendFeedback(new TranslatableText("command.custommodel.select", 1, model));
            return 1;
        }).then(ArgumentBuilders.argument("targets", EntityArgumentType.players()).executes(context -> {
            List<GameProfile> players = ((IClientEntitySelector) (Object) context.getArgument("targets", EntitySelector.class)).getPlayers(context.getSource());
            String model = context.getArgument("model", String.class);
            for (GameProfile player : players)
                CustomModelClient.selectModel(player, model);
            context.getSource().sendFeedback(new TranslatableText("command.custommodel.select", players.size(), model));
            return players.size();
        })))).then(ArgumentBuilders.literal("refresh").executes(context -> {
            CustomModel.refreshModelList();
            context.getSource().sendFeedback(new TranslatableText("command.custommodel.listmodels", CustomModel.models.size()));
            return 1;
        })));
    }

    public static interface IClientEntitySelector {
        List<GameProfile> getPlayers(CommandSource commandSource) throws CommandSyntaxException;
    }

    public static class ModelArgumentType implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            return reader.readUnquotedString();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            S source = context.getSource();
            if (source instanceof CommandSource) {
                return CommandSource.suggestMatching(CustomModel.getModelIdList(), builder);
            } else return Suggestions.empty();
        }
    }
}
