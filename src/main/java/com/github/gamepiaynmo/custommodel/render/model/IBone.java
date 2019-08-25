package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.render.PlayerBones;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.util.Quaternion;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.sun.javafx.geom.Vec2d;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import java.util.function.Function;
import java.util.function.Supplier;

public interface IBone {
    public Vector3 getPosition();
    public Vector3 getRotation();
    public Vector3 getScale();
    public Vec2d getTextureSize();
    public Supplier<Identifier> getTexture();
    public IBone getParent();
    public PlayerBones getPlayerBone();
    public String getId();
    default public boolean isVisible() { return true; }

    default public Quaternion getQuaternion() {
        Vector3 rotation = getRotation();
        return new Quaternion().setEulerAnglesRad(rotation.x, rotation.y, rotation.z);
    }
    default public Matrix4 getTransform() {
        return new Matrix4().setToScaling(getScale()).rotate(getQuaternion()).translate(getPosition().scl(0.0625));
    }
}
