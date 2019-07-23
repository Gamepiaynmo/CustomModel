package com.github.gamepiaynmo.custommodel.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public MixinPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher_1, PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
        super(entityRenderDispatcher_1, entityModel_1, float_1);
    }

    @Overwrite
    public void method_4215(AbstractClientPlayerEntity playerEntity, double offX, double offY, double offZ, float rotYaw, float partial) {
        super.method_4054(playerEntity, offX, offY, offZ, rotYaw, partial);
    }
}
