package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.util.Matrix4;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;

public class RenderContext {
    public EntityLivingBase currentEntity;
    public RenderParameter currentParameter;
    public ModelPlayer currentModel;
    public CustomJsonModel currentJsonModel;
    public Matrix4 currentInvTransform;

    private boolean isPlayer;

    public void setEntity(EntityLivingBase entity) {
        currentEntity = entity;
        isPlayer = entity instanceof AbstractClientPlayer;
    }

    public void setPlayer(AbstractClientPlayer player) {
        currentEntity = player;
        isPlayer = true;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public AbstractClientPlayer getPlayer() {
        return isPlayer ? (AbstractClientPlayer) currentEntity : null;
    }

    public EntityLivingBase getEntity() {
        return currentEntity;
    }
}
