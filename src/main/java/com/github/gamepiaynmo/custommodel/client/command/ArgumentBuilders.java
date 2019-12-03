package com.github.gamepiaynmo.custommodel.client.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

/**
 * Provides replacements for the ServerCommandManager argument methods.
 */
public final class ArgumentBuilders {
    private ArgumentBuilders() {}

    public static LiteralArgumentBuilder<CottonClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CottonClientCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}
