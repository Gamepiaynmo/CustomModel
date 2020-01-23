package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.render.ICustomPlayerRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.FirstPersonRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonRenderer.class)
public class MixinFirstPersonRenderer {
    @Inject(method = "renderFirstPersonItem(F)V", at = @At(value = "INVOKE", target = "disableRescaleNormal"))
    public void renderFirstPersonItem(float f, CallbackInfo info) {
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        PlayerEntityRenderer renderer = MinecraftClient.getInstance().getEntityRenderManager().getRenderer(playerEntity);
        ((ICustomPlayerRenderer) renderer).renderFirstPerson(playerEntity);
    }
}
