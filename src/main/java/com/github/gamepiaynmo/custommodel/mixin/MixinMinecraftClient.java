package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.render.IPartial;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient implements IPartial {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
    public void clearModelPacks(Screen screen, CallbackInfo info) {
        CustomModelClient.manager.clearModels();
        CustomModelClient.initServerStatus();
    }

    @Shadow
    private boolean paused;
    @Shadow
    private float pausedTickDelta;
    @Shadow
    private RenderTickCounter renderTickCounter;

    @Override
    public float getPartial() {
        return paused ? pausedTickDelta : renderTickCounter.tickDelta;
    }
}
