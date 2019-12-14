package com.github.gamepiaynmo.custommodel.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.github.gamepiaynmo.custommodel.client.command.ClientCommands;
import com.github.gamepiaynmo.custommodel.client.command.CommandCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.CommandTreeS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinNetworkHandler {
    @Shadow private CommandDispatcher<CommandSource> commandDispatcher;

    @Inject(method = "onCommandTree", at = @At("RETURN"))
    private void onCommandTree(CommandTreeS2CPacket packet, CallbackInfo info) {
        addCommands();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(MinecraftClient mc, Screen screen, ClientConnection cc, GameProfile gp, CallbackInfo info) {
        addCommands();
        CommandCache.build();
    }

    @Unique
    @SuppressWarnings("unchecked")
    private void addCommands() {
        ClientCommands.getPlugins().forEach(c -> c.registerCommands((CommandDispatcher) commandDispatcher));
    }
}
