package com.github.gamepiaynmo.custommodel.client.command;

import net.minecraft.server.command.CommandSource;
import net.minecraft.text.Text;

/**
 * Alternative to {@code ServerCommandSource}.
 * Contains methods for sending messages to the player.
 *
 * (since 0.4.1) This interface is implemented on {@code ClientCommandSource} instances.
 */
public interface CottonClientCommandSource extends CommandSource {
    void sendFeedback(Text text);
    void sendError(Text text);
}
