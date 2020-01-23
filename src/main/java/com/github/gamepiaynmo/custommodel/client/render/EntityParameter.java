package com.github.gamepiaynmo.custommodel.client.render;

import net.minecraft.entity.LivingEntity;

public class EntityParameter {
    float bodyYaw;
    float yaw;
    float pitch;
    float headYaw;

    public EntityParameter(LivingEntity entity) {
        bodyYaw = entity.field_6283;
        yaw = entity.yaw;
        pitch = entity.pitch;
        headYaw = entity.headYaw;
    }

    public void apply(LivingEntity entity) {
        entity.field_6283 = bodyYaw;
        entity.yaw = yaw;
        entity.pitch = pitch;
        entity.headYaw = headYaw;
    }
}
