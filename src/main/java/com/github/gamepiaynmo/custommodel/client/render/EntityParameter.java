package com.github.gamepiaynmo.custommodel.client.render;

import net.minecraft.entity.EntityLivingBase;

public class EntityParameter {
    public float bodyYaw;
    public float yaw;
    public float pitch;
    public float headYaw;

    public EntityParameter(EntityLivingBase entity) {
        bodyYaw = entity.renderYawOffset;
        yaw = entity.rotationYaw;
        pitch = entity.rotationPitch;
        headYaw = entity.rotationYawHead;
    }

    public void apply(EntityLivingBase entity) {
        entity.renderYawOffset = bodyYaw;
        entity.rotationYaw = yaw;
        entity.rotationPitch = pitch;
        entity.rotationYawHead = headYaw;
    }
}
