package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.util.Matrix4;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

public class RenderContext {
    public LivingEntity currentEntity;
    public RenderParameter currentParameter;
    public PlayerEntityModel currentModel;
    public CustomJsonModel currentJsonModel;
    public Matrix4 currentInvTransform;
    public boolean isInvisible;
    public boolean renderEmissive;

    private boolean isPlayer;

    public void setEntity(LivingEntity entity) {
        currentEntity = entity;
        isPlayer = entity instanceof AbstractClientPlayerEntity;
    }

    public void setPlayer(AbstractClientPlayerEntity player) {
        currentEntity = player;
        isPlayer = true;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public AbstractClientPlayerEntity getPlayer() {
        return isPlayer ? (AbstractClientPlayerEntity) currentEntity : null;
    }

    public LivingEntity getEntity() {
        return currentEntity;
    }
}
