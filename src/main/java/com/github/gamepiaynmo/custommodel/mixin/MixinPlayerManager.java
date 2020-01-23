package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void onPlayerConnect(ClientConnection clientConnection, ServerPlayerEntity serverPlayerEntity, CallbackInfo info) {
        CustomModel.onPlayerLoggedIn(serverPlayerEntity);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void remove(ServerPlayerEntity serverPlayerEntity, CallbackInfo info) {
        CustomModel.onPlayerLoggedOut(serverPlayerEntity);
    }
}
