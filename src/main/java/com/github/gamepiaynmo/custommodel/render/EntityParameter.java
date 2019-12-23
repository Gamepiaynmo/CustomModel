package com.github.gamepiaynmo.custommodel.render;

import net.minecraft.entity.EntityLivingBase;

public class EntityParameter {
    float bodyYaw;
    float yaw;
    float pitch;
    float prevHeadYaw;
    float headYaw;

    public EntityParameter(EntityLivingBase entity) {
        bodyYaw = entity.renderYawOffset;
        yaw = entity.rotationYaw;
        pitch = entity.rotationPitch;
        prevHeadYaw = entity.prevRotationYawHead;
        headYaw = entity.rotationYawHead;
    }

    public void apply(EntityLivingBase entity) {
        entity.renderYawOffset = bodyYaw;
        entity.rotationYaw = yaw;
        entity.rotationPitch = pitch;
        entity.prevRotationYawHead = prevHeadYaw;
        entity.rotationYawHead = headYaw;
    }
}
