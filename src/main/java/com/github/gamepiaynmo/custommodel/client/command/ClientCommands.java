package com.github.gamepiaynmo.custommodel.client.command;

import com.google.common.collect.ImmutableList;

public final class ClientCommands {
    public static ImmutableList<ClientCommandPlugin> getPlugins() {
        return ImmutableList.of(new Command());
    }
}
