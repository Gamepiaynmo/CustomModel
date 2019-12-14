package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class MixinCamera {
    @Shadow
    private float cameraY;
    @Shadow
    private float lastCameraY;
    @Shadow
    private Entity focusedEntity;

    @Inject(method = "updateEyeHeight(V)V", at = @At("HEAD"), cancellable = true)
    public void updateEyeHeight(CallbackInfo info) {
        if (focusedEntity != null && focusedEntity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) focusedEntity;
            ModelPack pack = CustomModelClient.getModelForPlayer(player);
            if (pack != null) {
                Float eyeHeight = pack.getModel().eyeHeightMap.get(player.getPose());
                if (eyeHeight != null) {
                    this.lastCameraY = this.cameraY;
                    this.cameraY += (eyeHeight - this.cameraY) * 0.5F;
                    info.cancel();
                }
            }
        }
    }
}
