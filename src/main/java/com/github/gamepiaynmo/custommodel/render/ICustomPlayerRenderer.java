package com.github.gamepiaynmo.custommodel.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public interface ICustomPlayerRenderer {
    void tick(AbstractClientPlayerEntity playerEntity);

    void renderCustom(AbstractClientPlayerEntity playerEntity, float yaw, float partial, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light);
}
