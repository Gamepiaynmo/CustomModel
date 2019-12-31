package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.entity.CustomModelNpc;
import com.github.gamepiaynmo.custommodel.entity.NpcHelper;
import com.github.gamepiaynmo.custommodel.mixin.RenderPlayerHandler;
import com.github.gamepiaynmo.custommodel.render.feature.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityCustomNpc;

import javax.annotation.Nullable;

public class RenderNpc extends RenderLivingBase<CustomModelNpc> {
    private final boolean slim;
    private final ModelPlayer modelPlayer;

    public RenderNpc(ModelPlayer model, boolean slim) {
        super(Minecraft.getMinecraft().getRenderManager(), model, 0.5f);
        this.slim = slim;
        this.modelPlayer = model;

        RenderContext context = RenderPlayerHandler.getContext();
        this.addLayer(new CustomBipedArmor(this, context));
        this.addLayer(new CustomHeldItem(this, context));
        this.addLayer(new CustomArrow(this, context));
        this.addLayer(new CustomHead(model.bipedHead, context));
        this.addLayer(new CustomElytra(this, context));
        this.addLayer(new CustomEmissive(context));
    }

    public ModelPlayer getModelPlayer() {
        return modelPlayer;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(CustomModelNpc entity) {
        return new ResourceLocation(entity.display.getSkinTexture());
    }

    @Override
    public void doRender(CustomModelNpc entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected void renderModel(CustomModelNpc entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        RenderPlayerHandler.renderPre(entitylivingbaseIn, modelPlayer);
        if (!RenderPlayerHandler.renderModel(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor))
            super.renderModel(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
    }
}
