package com.github.gamepiaynmo.custommodel.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.github.gamepiaynmo.custommodel.client.command.ClientCommands;
import com.github.gamepiaynmo.custommodel.client.command.CottonClientCommandSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;

@Environment(EnvType.CLIENT)
public final class CommandCache {
	private CommandCache() {}

	private static final CommandDispatcher<CottonClientCommandSource> DISPATCHER = new CommandDispatcher<>();

	public static void build() {
		ClientCommands.getPlugins().forEach(provider -> provider.registerCommands(DISPATCHER));
	}

	public static int execute(String input, CottonClientCommandSource source) throws CommandSyntaxException {
		return DISPATCHER.execute(input, source);
	}

	public static boolean hasCommand(String name) {
		return DISPATCHER.findNode(Collections.singleton(name)) != null;
	}
}
