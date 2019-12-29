package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.render.RenderContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public class CustomEmissive extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private final RenderContext context;
    public CustomEmissive(LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> livingEntityRenderer_1, RenderContext context) {
        super(livingEntityRenderer_1);
        this.context = context;
    }

    @Override
    public void render(AbstractClientPlayerEntity var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
        context.renderEmissive = true;
        context.currentJsonModel.renderEmissive(context);
        context.renderEmissive = false;
    }

    @Override
    public boolean hasHurtOverlay() {
        return false;
    }
}
