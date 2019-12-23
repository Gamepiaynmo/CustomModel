package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.mixin.RenderPlayerHandler;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class CustomModelPlayer extends ModelPlayer {
    public CustomModelPlayer(float modelSize, boolean smallArmsIn) {
        super(modelSize, smallArmsIn);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        if (entityIn instanceof EntityLivingBase)
            RenderPlayerHandler.render((EntityLivingBase) entityIn);
    }
}
