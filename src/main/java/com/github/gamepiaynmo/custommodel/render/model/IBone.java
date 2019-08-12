package com.github.gamepiaynmo.custommodel.render.model;

import com.github.gamepiaynmo.custommodel.render.PlayerBones;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.util.Quaternion;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.sun.javafx.geom.Vec2d;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public interface IBone {
    public Vector3 getPosition(PlayerEntityModel model);
    public Vector3 getRotation(PlayerEntityModel model);
    public Vector3 getScale(PlayerEntityModel model);
    public Vec2d getTextureSize();
    public ModelPack.TextureGetter getTexture();
    public IBone getParent();
    public PlayerBones getPlayerBone();
    public String getId();

    default public Quaternion getQuaternion(PlayerEntityModel model) {
        Vector3 rotation = getRotation(model);
        return new Quaternion().setEulerAnglesRad(rotation.x, rotation.y, rotation.z);
    }
    default public Matrix4 getTransform(PlayerEntityModel model) {
        return new Matrix4().setToScaling(getScale(model)).rotate(getQuaternion(model)).translate(getPosition(model).scl(0.0625));
    }
}
