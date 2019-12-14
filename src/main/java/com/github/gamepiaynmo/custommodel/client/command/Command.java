package com.github.gamepiaynmo.custommodel.client.command;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

import java.io.File;
import java.util.List;

public class Command implements ClientCommandPlugin {
    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
        dispatcher.register(ArgumentBuilders.literal(CustomModel.MODID
        ).then(ArgumentBuilders.literal("reload").then(ArgumentBuilders.argument("targets", EntityArgumentType.players()).executes(context -> {
            try {
                List<GameProfile> players = ((IClientEntitySelector) (Object) context.getArgument("targets", EntitySelector.class)).getPlayers(context.getSource());
                players.forEach(CustomModelClient::clearModel);
                return players.size();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }))).then(ArgumentBuilders.literal("list").executes(context -> {
            List<File> fileList = Lists.newArrayList(new File(CustomModel.MODEL_DIR).listFiles());
            context.getSource().sendFeedback(new TranslatableText("command.custommodel.listmodels",
                    fileList.size(), Texts.join(fileList, (file) -> {
                return new LiteralText(file.getName());
            })));
            return fileList.size();
        })));
    }

    public static interface IClientEntitySelector {
        List<GameProfile> getPlayers(CommandSource commandSource) throws CommandSyntaxException;
    }
}
