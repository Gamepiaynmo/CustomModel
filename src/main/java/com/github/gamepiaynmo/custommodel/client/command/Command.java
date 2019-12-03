package com.github.gamepiaynmo.custommodel.client.command;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandSource;

import java.util.List;

public class Command implements ClientCommandPlugin {
    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
        dispatcher.register(ArgumentBuilders.literal(CustomModel.MODID).requires((source) -> {
            return true;
        }).then(ArgumentBuilders.literal("reload").then(ArgumentBuilders.argument("targets", EntityArgumentType.players()).executes(
                context -> {
                    try {
                        List<GameProfile> players = ((IClientEntitySelector) (Object) context.getArgument("targets", EntitySelector.class)).getPlayers(context.getSource());
                        players.forEach(CustomModelClient::clearModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 1;
                })
        )));
    }

    public static interface IClientEntitySelector {
        List<GameProfile> getPlayers(CommandSource commandSource) throws CommandSyntaxException;
    }
}
