package com.github.gamepiaynmo.custommodel.render;

import net.minecraft.entity.LivingEntity;

public class EntityParameter {
    float bodyYaw;
    float yaw;
    float pitch;
    float prevHeadYaw;
    float headYaw;

    public EntityParameter(LivingEntity entity) {
        bodyYaw = entity.field_6283;
        yaw = entity.yaw;
        pitch = entity.pitch;
        prevHeadYaw = entity.prevHeadYaw;
        headYaw = entity.headYaw;
    }

    public void apply(LivingEntity entity) {
        entity.field_6283 = bodyYaw;
        entity.yaw = yaw;
        entity.pitch = pitch;
        entity.prevHeadYaw = prevHeadYaw;
        entity.headYaw = headYaw;
    }
}
