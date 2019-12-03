package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.render.CustomPlayerEntityRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public MixinPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher_1, PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
        super(entityRenderDispatcher_1, entityModel_1, float_1);
    }

    @Inject(method = "setModelPose(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    private void setModelPose(AbstractClientPlayerEntity playerEntity, CallbackInfo info) {
        if ((Object) this instanceof CustomPlayerEntityRenderer) {
            if (((CustomPlayerEntityRenderer) (Object) this).disableSetModelPose)
                info.cancel();
        }
    }
}
