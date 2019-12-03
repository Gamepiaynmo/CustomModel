package com.github.gamepiaynmo.custommodel.client.command;

import com.mojang.brigadier.CommandDispatcher;

public interface ClientCommandPlugin {
	void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher);
}
