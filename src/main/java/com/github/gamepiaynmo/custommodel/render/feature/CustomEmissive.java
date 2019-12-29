package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.render.RenderContext;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public class CustomEmissive implements LayerRenderer<EntityLivingBase> {
    private final RenderContext context;
    public CustomEmissive(RenderContext context) {
        this.context = context;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        context.renderEmissive = true;
        context.currentJsonModel.renderEmissive(context);
        context.renderEmissive = false;
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
