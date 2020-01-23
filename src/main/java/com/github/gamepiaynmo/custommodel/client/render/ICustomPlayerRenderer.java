package com.github.gamepiaynmo.custommodel.client.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;

public interface ICustomPlayerRenderer {
    void tick(LivingEntity entity);

    boolean renderModel(AbstractClientPlayerEntity playerEntity, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6);

    void renderFirstPerson(ClientPlayerEntity playerEntity);
}
